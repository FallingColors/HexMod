from __future__ import annotations

import json
from dataclasses import InitVar
from functools import total_ordering
from pathlib import Path
from typing import Any, Callable, Self

from pydantic import ValidationInfo, model_validator
from pydantic.dataclasses import dataclass
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.utils import (
    DEFAULT_CONFIG,
    HexDocModel,
    ItemStack,
    ModResourceLoader,
    Properties,
    ResourceLocation,
)
from hexdoc.utils.deserialize import (
    cast_or_raise,
    decode_and_flatten_json_dict,
    isinstance_or_raise,
)
from hexdoc.utils.model import HexDocValidationContext
from hexdoc.utils.types import without_suffix


@total_ordering
class LocalizedStr(HexDocModel):
    """Represents a string which has been localized."""

    key: str
    value: str

    @classmethod
    def skip_i18n(cls, key: str) -> Self:
        """Returns an instance of this class with `value = key`."""
        return cls(key=key, value=key)

    @classmethod
    def with_value(cls, value: str) -> Self:
        """Returns an instance of this class with an empty key."""
        return cls(key="", value=value)

    @model_validator(mode="wrap")
    @classmethod
    def _check_localize(
        cls,
        value: str | Any,
        handler: ModelWrapValidatorHandler[Self],
        info: ValidationInfo,
    ):
        # TODO: if we need LocalizedStr to work as a dict key, add another check which
        # returns cls.skip_i18n(value) if info.context is falsy
        if not isinstance(value, str):
            return handler(value)

        context = cast_or_raise(info.context, I18nContext)
        return cls._localize(context.i18n, value)

    @classmethod
    def _localize(cls, i18n: I18n, key: str) -> Self:
        return i18n.localize(key)

    def map(self, fn: Callable[[str], str]) -> Self:
        """Returns a copy of this object with `new.value = fn(old.value)`."""
        return self.model_copy(update={"value": fn(self.value)})

    def __repr__(self) -> str:
        return self.value

    def __str__(self) -> str:
        return self.value

    def __eq__(self, other: Self | str | Any):
        match other:
            case LocalizedStr():
                return self.value == other.value
            case str():
                return self.value == other
            case _:
                return super().__eq__(other)

    def __lt__(self, other: Self | str):
        match other:
            case LocalizedStr():
                return self.value < other.value
            case str():
                return self.value < other


class LocalizedItem(LocalizedStr):
    @classmethod
    def _localize(cls, i18n: I18n, key: str) -> Self:
        return i18n.localize_item(key)


@dataclass(config=DEFAULT_CONFIG)
class I18n:
    """Handles localization of strings."""

    props: InitVar[Properties]
    loader: InitVar[ModResourceLoader]
    enabled: bool

    lookup: dict[str, LocalizedStr] | None = None

    def __post_init__(self, props: Properties, loader: ModResourceLoader):
        # skip loading the files if we don't need to
        self.lookup = None
        if not self.enabled:
            return

        # load and deserialize
        # TODO: load ALL of the i18n files, return dict[str, _Lookup] | None
        # or maybe dict[(str, str), LocalizedStr]
        # we could also use that to ensure all i18n files have the same set of keys
        raw_lookup: dict[str, str] = {}
        for _, _, data in loader.load_resources(
            type="assets",
            folder="lang",
            base_id=ResourceLocation("*", ""),
            glob=[
                f"{props.i18n.default_lang}.json",
                f"{props.i18n.default_lang}.json5",
                f"{props.i18n.default_lang}.flatten.json",
                f"{props.i18n.default_lang}.flatten.json5",
            ],
            decode=decode_and_flatten_json_dict,
            export=self._export,
        ):
            raw_lookup |= data

        # validate and insert
        self.lookup = {
            key: LocalizedStr(key=key, value=value.replace("%%", "%"))
            for key, value in raw_lookup.items()
        }

    def _export(self, path: Path, value: dict[str, str]):
        path = without_suffix(path).with_suffix(".json")

        try:
            current = decode_and_flatten_json_dict(path.read_text("utf-8"))
        except FileNotFoundError:
            current = {}

        with path.open("w", encoding="utf-8") as f:
            json.dump(current | value, f)

    def localize(self, *keys: str, default: str | None = None) -> LocalizedStr:
        """Looks up the given string in the lang table if i18n is enabled. Otherwise,
        returns the original key.

        If multiple keys are provided, returns the value of the first key which exists.
        That is, subsequent keys are treated as fallbacks for the first.

        Raises KeyError if i18n is enabled and default is None but the key has no
        corresponding localized value.
        """

        # if i18n is disabled, just return the key
        if self.lookup is None:
            return LocalizedStr.skip_i18n(keys[0])

        # for a single key, look it up
        if len(keys) == 1:
            if default is not None:
                return self.lookup.get(keys[0], LocalizedStr.skip_i18n(default))
            # raises if not found
            return self.lookup[keys[0]]

        # for a list/tuple of keys, return the first one that matches (by recursing)
        for current_key in keys[:-1]:
            assert isinstance_or_raise(current_key, str)
            try:
                return self.localize(current_key)
            except KeyError:
                continue

        return self.localize(keys[-1], default=default)

    def localize_pattern(self, op_id: ResourceLocation) -> LocalizedStr:
        """Localizes the given pattern id (internal name, eg. brainsweep).

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        # prefer the book-specific translation if it exists
        # TODO: should this use op_id.namespace anywhere?
        return self.localize(
            f"hexcasting.action.book.{op_id}",
            f"hexcasting.action.{op_id}",
        )

    def localize_item(self, item: ItemStack | str) -> LocalizedItem:
        """Localizes the given item resource name.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        if isinstance(item, str):
            item = ItemStack.from_str(item)

        localized = self.localize(
            item.i18n_key("block"),
            item.i18n_key(),
        )
        return LocalizedItem(key=localized.key, value=localized.value)

    def localize_key(self, key: str) -> LocalizedStr:
        return self.localize(f"key.{key}")


class I18nContext(HexDocValidationContext):
    i18n: I18n
