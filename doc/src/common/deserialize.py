import json
from pathlib import Path
from typing import Self

from common.types import LocalizedStr
from serde import SerdeError
from serde.json import from_json

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


class FromJson:
    """Helper methods for JSON-deserialized dataclasses."""

    @classmethod
    def from_json(cls, string: str | bytes) -> Self:
        """Deserializes the given string into this class."""
        try:
            return from_json(cls, string)
        except SerdeError as e:
            e.add_note(str(string))
            raise

    @classmethod
    def load(cls, path: Path) -> Self:
        """Reads and deserializes the JSON file at the given path."""
        return cls.from_json(path.read_text("utf-8"))
