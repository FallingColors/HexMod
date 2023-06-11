import json
import re
from dataclasses import InitVar, dataclass, field
from pathlib import Path
from typing import Self


# subclass instead of newtype so it exists at runtime, so we can use isinstance
class LocalizedStr(str):
    """Represents a string which has been localized."""

    def __new__(cls, s: str) -> Self:
        return str.__new__(cls, s)


# TODO: move to config
_EXTRA_I18N = {
    "item.minecraft.amethyst_shard": LocalizedStr("Amethyst Shard"),
    "item.minecraft.budding_amethyst": LocalizedStr("Budding Amethyst"),
    "block.hexcasting.slate": LocalizedStr("Blank Slate"),
}


@dataclass
class I18n:
    """Handles localization of strings."""

    resource_dir: Path
    modid: str
    default_lang: str

    enabled: InitVar[bool]
    extra_i18n: InitVar[dict[str, LocalizedStr] | None] = None

    _i18n: dict[str, LocalizedStr] | None = field(default=None)

    def __post_init__(self, enabled: bool, extra_i18n: dict[str, LocalizedStr] | None):
        if not enabled:
            return

        # load and deserialize
        # TODO: load ALL of the i18n files, return dict[str, dict[str, LocalizedStr]]
        # or maybe dict[(str, str), LocalizedStr]
        # we could also use that to ensure all i18n files have the same set of keys
        path = self.dir / f"{self.default_lang}.json"
        self._i18n = json.loads(path.read_text("utf-8"))
        self._i18n.update(_EXTRA_I18N)
        if extra_i18n:
            self._i18n.update(extra_i18n)

        # type-checking
        # TODO: there's probably a library we can use to do this for us
        assert isinstance(
            self._i18n, dict
        ), f"Unexpected top-level type `{type(self._i18n)}` in i18n: {path}"
        for k, v in self._i18n.items():
            assert isinstance(k, str), f"Unexpected key type `{type(k)}` in i18n: {k}"
            assert isinstance(v, str), f"Unexpected value type `{type(v)}` in i18n: {v}"

    @property
    def dir(self) -> Path:
        """eg. `resources/assets/hexcasting/lang`"""
        return self.resource_dir / "assets" / self.modid / "lang"

    def localize(
        self,
        key: str,
        default: str | None = None,
        skip_errors: bool = False,
    ) -> LocalizedStr:
        """Looks up the given string in the lang table if i18n is enabled.
        Otherwise, returns the original key.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        if self._i18n is None:
            return LocalizedStr(key.replace("%%", "%"))

        if default is not None:
            localized = self._i18n.get(key, default)
        elif skip_errors:
            localized = self._i18n.get(key, key)
        else:
            # raises if not found
            localized = self._i18n[key]

        return LocalizedStr(localized.replace("%%", "%"))

    def localize_pattern(self, op_id: str, skip_errors: bool = False) -> LocalizedStr:
        """Localizes the given pattern id (internal name, eg. brainsweep).

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        try:
            # prefer the book-specific translation if it exists
            # don't pass skip_errors here because we need to catch it below
            return self.localize(f"hexcasting.spell.book.{op_id}")
        except KeyError:
            return self.localize(f"hexcasting.spell.{op_id}", skip_errors=skip_errors)

    def localize_item(self, item: str, skip_errors: bool = False) -> LocalizedStr:
        """Localizes the given item resource name.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        # FIXME: hack
        item = re.sub(r"{.*", "", item.replace(":", "."))
        try:
            return self.localize(f"block.{item}")
        except KeyError:
            return self.localize(f"item.{item}", skip_errors=skip_errors)
