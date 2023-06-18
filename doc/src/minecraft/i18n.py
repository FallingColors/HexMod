import re
from dataclasses import InitVar, dataclass
from pathlib import Path
from typing import Collection, Iterable, Self

from common.deserialize import load_json
from common.properties import Properties
from minecraft.resource import ItemStack, ResourceLocation


# subclass instead of newtype so it exists at runtime, so we can use isinstance
class LocalizedStr(str):
    """Represents a string which has been localized."""


class LocalizedItem(LocalizedStr):
    pass


class LocalizedRecipeResult(LocalizedStr):
    pass


I18nLookup = dict[str, LocalizedStr]


@dataclass
class I18n:
    """Handles localization of strings."""

    props: Properties
    enabled: InitVar[bool]

    def __post_init__(self, enabled: bool):
        # skip loading the files if we don't need to
        self._lookup: I18nLookup | None = None
        if not enabled:
            return

        # load, deserialize, validate
        # TODO: load ALL of the i18n files, return dict[str, _Lookup] | None
        # or maybe dict[(str, str), LocalizedStr]
        # we could also use that to ensure all i18n files have the same set of keys
        path = self.dir / f"{self.props.lang}.json"
        _lookup = load_json(path)
        if self.props.i18n.extra:
            _lookup.update(self.props.i18n.extra)

        # validate fields
        # TODO: there's probably a library we can use to do this for us
        for k, v in _lookup.items():
            assert isinstance(k, str), f"Unexpected key type `{type(k)}` in {path}: {k}"
            assert isinstance(
                v, str
            ), f"Unexpected value type `{type(v)}` in {path}: {v}"

        self._lookup = _lookup

    @property
    def dir(self) -> Path:
        """eg. `resources/assets/hexcasting/lang`"""
        return self.props.resources / "assets" / self.props.modid / "lang"

    def localize(
        self,
        key: str | list[str] | tuple[str, ...],
        default: str | None = None,
        skip_errors: bool = False,
    ) -> LocalizedStr:
        """Looks up the given string in the lang table if i18n is enabled.
        Otherwise, returns the original key.

        If a tuple/list of keys is provided, returns the value of the first key which
        exists. That is, subsequent keys are treated as fallbacks for the first.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no
        corresponding localized value.
        """

        assert isinstance(key, (str, list, tuple))

        if self._lookup is None:
            # if i18n is disabled, just return the key
            if not isinstance(key, str):
                key = key[0]
            localized = key
        elif isinstance(key, str):
            # for a single key, look it up
            if default is not None:
                localized = self._lookup.get(key, default)
            elif skip_errors:
                localized = self._lookup.get(key, key)
            else:
                # raises if not found
                localized = self._lookup[key]
        else:
            # for a list/tuple of keys, return the first one that matches (by recursing)
            for current_key in key[:-1]:
                assert isinstance(current_key, str)
                try:
                    return self.localize(current_key)
                except KeyError:
                    continue
            return self.localize(key[-1], default, skip_errors)

        return LocalizedStr(localized.replace("%%", "%"))

    def localize_pattern(
        self,
        op_id: ResourceLocation,
        skip_errors: bool = False,
    ) -> LocalizedStr:
        """Localizes the given pattern id (internal name, eg. brainsweep).

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        # prefer the book-specific translation if it exists
        # TODO: should this use op_id.namespace anywhere?
        return self.localize(
            (f"hexcasting.spell.book.{op_id.path}", f"hexcasting.spell.{op_id.path}"),
            skip_errors=skip_errors,
        )

    def localize_item(
        self,
        item: ItemStack,
        skip_errors: bool = False,
    ) -> LocalizedItem:
        """Localizes the given item resource name.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        return LocalizedItem(
            self.localize(
                (item.i18n_key("block"), item.i18n_key()), skip_errors=skip_errors
            )
        )
