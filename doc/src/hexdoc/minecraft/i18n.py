from __future__ import annotations

import json
import logging
from collections import defaultdict
from functools import total_ordering
from pathlib import Path
from typing import Any, Callable, Self

from pydantic import ValidationInfo, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.utils import HexdocModel, ItemStack, ModResourceLoader, ResourceLocation
from hexdoc.utils.deserialize import (
    cast_or_raise,
    decode_and_flatten_json_dict,
    isinstance_or_raise,
)
from hexdoc.utils.path import replace_suffixes
from hexdoc.utils.resource_loader import LoaderContext


@total_ordering
class LocalizedStr(HexdocModel):
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
        # NOTE: if we need LocalizedStr to work as a dict key, add another check which
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


class I18n(HexdocModel):
    """Handles localization of strings."""

    lookup: dict[str, LocalizedStr] | None
    lang: str
    allow_missing: bool

    @classmethod
    def list_all(cls, loader: ModResourceLoader):
        return set(id.path for _, id, _ in cls._load_lang_resources(loader))

    @classmethod
    def load_all(cls, loader: ModResourceLoader, *, allow_missing: bool):
        # lang -> (key -> value)
        lookups = defaultdict[str, dict[str, LocalizedStr]](dict)

        for _, lang_id, data in cls._load_lang_resources(loader):
            lookups[lang_id.path] |= {
                key: LocalizedStr(key=key, value=value.replace("%%", "%"))
                for key, value in data.items()
            }

        return {
            lang: cls(lookup=lookup, lang=lang, allow_missing=allow_missing)
            for lang, lookup in lookups.items()
        }

    @classmethod
    def load(cls, loader: ModResourceLoader, *, lang: str, allow_missing: bool):
        lookup = dict[str, LocalizedStr]()

        for _, _, data in cls._load_lang_resources(loader, lang):
            lookup |= {
                key: LocalizedStr(key=key, value=value.replace("%%", "%"))
                for key, value in data.items()
            }

        return cls(lookup=lookup, lang=lang, allow_missing=allow_missing)

    @classmethod
    def _load_lang_resources(cls, loader: ModResourceLoader, lang: str = "*"):
        return loader.load_resources(
            "assets",
            namespace="*",
            folder="lang",
            glob=[
                f"{lang}.json",
                f"{lang}.json5",
                f"{lang}.flatten.json",
                f"{lang}.flatten.json5",
            ],
            decode=decode_and_flatten_json_dict,
            export=cls._export,
        )

    @classmethod
    def _export(
        cls,
        new: dict[str, str],
        current: dict[str, str] | None,
        path: Path,
    ):
        data = json.dumps((current or {}) | new)
        path = replace_suffixes(path, ".json")
        return data, path

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
            key = keys[0]
            if default is not None:
                return self.lookup.get(key, LocalizedStr.skip_i18n(default))

            try:
                return self.lookup[key]
            except KeyError as e:
                if not self.allow_missing:
                    e.add_note(f"Lang: {self.lang}")
                    raise

                logging.getLogger(__name__).warning(
                    f"No translation in {self.lang} for key {key}"
                )
                return LocalizedStr.skip_i18n(key)

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


class I18nContext(LoaderContext):
    i18n: I18n
