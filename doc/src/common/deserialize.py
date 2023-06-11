# pyright: reportUnknownArgumentType=false, reportUnknownMemberType=false

import dataclasses
from abc import ABC, abstractmethod
from pathlib import Path
from typing import Any, Self, dataclass_transform

import serde
from serde import SerdeError
from serde.json import from_json


class FromStr(ABC):
    """Base class for types which are deserialized from a string."""

    @classmethod
    @abstractmethod
    def from_str(cls, s: str) -> Self:
        ...

    @classmethod
    def field(cls, *args: Any, factory: str | None = None, **kwargs: Any) -> Any:
        """Helper method for using this as a dataclass field. You must use this method if
        you're putting this in a serde class.

        If `factory` is provided, `default_factory` will be set to the following:
            `lambda: cls.from_str(factory)`
        """
        if factory is not None:
            kwargs["default_factory"] = lambda: cls.from_str(factory)
        return serde.field(*args, deserializer=cls.from_str, **kwargs)


# dataclass_transform ensures type checkers work properly with these field specifiers
@dataclass_transform(field_specifiers=(dataclasses.field, FromStr.field, serde.field))
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
