from pathlib import Path
from typing import Self

from serde import SerdeError
from serde.json import from_json


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
