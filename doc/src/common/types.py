from __future__ import annotations

import string
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import TYPE_CHECKING, Any, Mapping, Protocol, Self, TypeVar

from common.deserialize import Castable

# circular imports are gross
if TYPE_CHECKING:
    from patchouli.book import Book
    from patchouli.category import Category
    from patchouli.entry import Entry
else:
    Book, Category, Entry = Any, Any, Any


class Color(str, Castable):
    """Newtype-style class representing a hexadecimal color.

    Inputs are coerced to lowercase `rrggbb`. Raises ValueError on invalid input.

    Valid formats, all of which would be converted to `0099ff`:
    - `#0099FF`
    - `#0099ff`
    - `#09F`
    - `#09f`
    - `0099FF`
    - `0099ff`
    - `09F`
    - `09f`
    """

    __slots__ = ()

    def __new__(cls, s: str) -> Self:
        assert isinstance(s, str), f"Expected str, got {type(s)}"

        color = s.removeprefix("#").lower()

        # 012 -> 001122
        if len(color) == 3:
            color = "".join(c + c for c in color)

        # length and character check
        if len(color) != 6 or any(c not in string.hexdigits for c in color):
            raise ValueError(f"invalid color code: {s}")

        return str.__new__(cls, color)


# subclass instead of newtype so it exists at runtime, so we can use isinstance
class LocalizedStr(str):
    """Represents a string which has been localized."""

    def __new__(cls, value: str) -> Self:
        # check the type because we use this while deserializing the i18n dict
        assert isinstance(value, str), f"Expected str, got {type(value)}"
        return str.__new__(cls, value)


class Sortable(ABC):
    """ABC for classes which can be sorted."""

    @property
    @abstractmethod
    def _cmp_key(self) -> Any:
        ...

    def __lt__(self, other: Any) -> bool:
        if isinstance(other, Sortable):
            return self._cmp_key < other._cmp_key
        return NotImplemented


_T = TypeVar("_T")

_T_Sortable = TypeVar("_T_Sortable", bound=Sortable)


def sorted_dict(d: Mapping[_T, _T_Sortable]) -> dict[_T, _T_Sortable]:
    return dict(sorted(d.items(), key=lambda item: item[1]))


class BookHelpers(ABC):
    """Shortcuts for types with a book field."""

    @property
    @abstractmethod
    def book(self) -> Book:
        ...

    @property
    def props(self):
        return self.book.props

    @property
    def i18n(self):
        return self.book.i18n
