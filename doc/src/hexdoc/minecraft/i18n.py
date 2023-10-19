from __future__ import annotations

import json
import logging
from collections import defaultdict
from functools import total_ordering
from typing import Any, Callable, Self

from pydantic import ValidationInfo, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.core.compat import HexVersion
from hexdoc.core.loader import LoaderContext, ModResourceLoader
from hexdoc.core.resource import ItemStack, ResourceLocation
from hexdoc.model import HexdocModel
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.deserialize.json import decode_and_flatten_json_dict


@total_ordering
class LocalizedStr(HexdocModel, frozen=True):
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


class LocalizedItem(LocalizedStr, frozen=True):
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
        # don't list languages which this particular mod doesn't support
        # eg. if Hex has translations for ru_ru but an addon doesn't
        return set(
            id.path
            for resource_dir, id, _ in cls._load_lang_resources(loader)
            if not resource_dir.external
        )

    @classmethod
    def load_all(cls, loader: ModResourceLoader, *, allow_missing: bool):
        # lang -> (key -> value)
        lookups = defaultdict[str, dict[str, LocalizedStr]](dict)
        internal_langs = set[str]()

        for resource_dir, lang_id, data in cls._load_lang_resources(loader):
            lang = lang_id.path
            lookups[lang] |= {
                key: LocalizedStr(key=key, value=value.replace("%%", "%"))
                for key, value in data.items()
            }
            if not resource_dir.external:
                internal_langs.add(lang)

        return {
            lang: cls(lookup=lookup, lang=lang, allow_missing=allow_missing)
            for lang, lookup in lookups.items()
            if lang in internal_langs
        }

    @classmethod
    def load(cls, loader: ModResourceLoader, *, lang: str, allow_missing: bool):
        lookup = dict[str, LocalizedStr]()
        is_internal = False

        for resource_dir, _, data in cls._load_lang_resources(loader, lang):
            lookup |= {
                key: LocalizedStr(key=key, value=value.replace("%%", "%"))
                for key, value in data.items()
            }
            if not resource_dir.external:
                is_internal = True

        if not is_internal:
            raise FileNotFoundError(
                f"Lang {lang} exists, but {loader.props.modid} does not support it"
            )

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
    def _export(cls, new: dict[str, str], current: dict[str, str] | None):
        return json.dumps((current or {}) | new)

    def localize(
        self,
        *keys: str,
        default: str | None = None,
        allow_missing: bool | None = None,
    ) -> LocalizedStr:
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

        for key in keys:
            if key in self.lookup:
                return self.lookup[key]

        if default is not None:
            return LocalizedStr.skip_i18n(default)

        message = f"No translation in {self.lang} for "
        if len(keys) == 1:
            message += f"key {keys[0]}"
        else:
            message += f"keys {keys}"

        if allow_missing is False:
            raise KeyError(message)

        logging.getLogger(__name__).error(message)
        return LocalizedStr.skip_i18n(keys[0])

    def localize_pattern(self, op_id: ResourceLocation) -> LocalizedStr:
        """Localizes the given pattern id (internal name, eg. brainsweep).

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        match HexVersion.get():
            case HexVersion.v0_11_x:
                key_group = "action"
            case HexVersion.v0_10_x | HexVersion.v0_9_x:
                key_group = "spell"

        # prefer the book-specific translation if it exists
        return self.localize(
            f"hexcasting.{key_group}.book.{op_id}",
            f"hexcasting.{key_group}.{op_id}",
        )

    def localize_item(self, item: str | ResourceLocation | ItemStack) -> LocalizedItem:
        """Localizes the given item resource name.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        match item:
            case str():
                item = ItemStack.from_str(item)
            case ResourceLocation(namespace=namespace, path=path):
                item = ItemStack(namespace=namespace, path=path)
            case _:
                pass

        localized = self.localize(
            item.i18n_key(),
            item.i18n_key("block"),
        )
        return LocalizedItem(key=localized.key, value=localized.value)

    def localize_key(self, key: str) -> LocalizedStr:
        if not key.startswith("key."):
            key = "key." + key
        return self.localize(key)

    def localize_item_tag(self, tag: ResourceLocation):
        localized = self.localize(
            f"tag.{tag.namespace}.{tag.path}",
            f"tag.item.{tag.namespace}.{tag.path}",
            f"tag.block.{tag.namespace}.{tag.path}",
        )
        return LocalizedStr(key=localized.key, value=f"Tag: {localized.value}")


class I18nContext(LoaderContext):
    i18n: I18n
