from dataclasses import InitVar, dataclass
from pathlib import Path

from common.deserialize import load_json_object
from common.properties import Properties
from common.types import LocalizedItem, LocalizedStr, isinstance_or_raise
from minecraft.resource import ItemStack, ResourceLocation


@dataclass
class I18n:
    """Handles localization of strings."""

    props: Properties
    enabled: InitVar[bool]

    def __post_init__(self, enabled: bool):
        # skip loading the files if we don't need to
        self._lookup: dict[str, LocalizedStr] | None = None
        if not enabled:
            return

        # load and deserialize
        # TODO: load ALL of the i18n files, return dict[str, _Lookup] | None
        # or maybe dict[(str, str), LocalizedStr]
        # we could also use that to ensure all i18n files have the same set of keys
        path = self.dir / self.props.i18n.filename
        raw_lookup = load_json_object(path) | (self.props.i18n.extra or {})

        # validate and insert
        self._lookup = {}
        for key, raw_value in raw_lookup.items():
            assert isinstance_or_raise(raw_value, str)
            self._lookup[key] = LocalizedStr(raw_value)

    @property
    def dir(self) -> Path:
        """eg. `resources/assets/hexcasting/lang`"""
        return self.props.resources_dir / "assets" / self.props.modid / "lang"

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

        assert isinstance_or_raise(key, (str, list[str], tuple[str, ...]))

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
                assert isinstance_or_raise(current_key, str)
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
            (f"hexcasting.spell.book.{op_id}", f"hexcasting.spell.{op_id}"),
            skip_errors=skip_errors,
        )

    def localize_item(
        self,
        item: ItemStack | str,
        skip_errors: bool = False,
    ) -> LocalizedItem:
        """Localizes the given item resource name.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        if isinstance(item, str):
            item = ItemStack.from_str(item)
        return LocalizedItem(
            self.localize(
                (item.i18n_key("block"), item.i18n_key()), skip_errors=skip_errors
            )
        )
