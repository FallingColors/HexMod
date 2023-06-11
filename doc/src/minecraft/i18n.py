import json
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

# TODO: load ALL of the i18n files, return dict[str, dict[str, LocalizedStr]]
# or maybe dict[(str, str), LocalizedStr]
# we could also use that to ensure all i18n files have the same set of keys
def load_i18n(path: Path) -> dict[str, LocalizedStr]:
    # load, deserialize, and type-check lang file
    # TODO: there's probably a library we can use to do this for us
    i18n: dict[str, LocalizedStr] = json.loads(path.read_text("utf-8"))
    i18n.update(_EXTRA_I18N)

    assert isinstance(i18n, dict), f"Unexpected top-level type `{type(i18n)}` in i18n"
    for k, v in i18n.items():
        assert isinstance(k, str), f"Unexpected key type `{type(k)}` in i18n: {k}"
        assert isinstance(v, str), f"Unexpected value type `{type(v)}` in i18n: {v}"

    return i18n
