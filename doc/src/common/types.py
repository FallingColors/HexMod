from __future__ import annotations

import string
from abc import ABC, abstractmethod
from typing import Any, Mapping, Protocol, Self, TypeGuard, TypeVar, get_origin

JSONDict = dict[str, "JSONValue"]

JSONValue = JSONDict | list["JSONValue"] | str | int | float | bool | None

_T = TypeVar("_T")

_DEFAULT_MESSAGE = "Expected any of {expected}, got {actual}: {value}"


# there may well be a better way to do this but i don't know what it is
def isinstance_or_raise(
    val: Any,
    class_or_tuple: type[_T] | tuple[type[_T], ...],
    message: str = _DEFAULT_MESSAGE,
) -> TypeGuard[_T]:
    """Usage: `assert isinstance_or_raise(val, str)`

    message placeholders: `{expected}`, `{actual}`, `{value}`
    """

    # convert generic types into the origin type
    if not isinstance(class_or_tuple, tuple):
        class_or_tuple = (class_or_tuple,)
    ungenericed_classes = tuple(get_origin(t) or t for t in class_or_tuple)

    if not isinstance(val, ungenericed_classes):
        # just in case the caller messed up the message formatting
        subs = {"expected": class_or_tuple, "actual": type(val), "value": val}
        try:
            raise TypeError(message.format(**subs))
        except KeyError:
            raise TypeError(_DEFAULT_MESSAGE.format(**subs))
    return True


class Castable:
    """Abstract base class for types with a constructor in the form `C(value) -> C`.

    Subclassing this ABC allows for automatic deserialization using Dacite.
    """


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

    def __new__(cls, value: str) -> Self:
        # this is a castable type hook but we hint str for usability
        assert isinstance_or_raise(value, str)

        color = value.removeprefix("#").lower()

        # 012 -> 001122
        if len(color) == 3:
            color = "".join(c + c for c in color)

        # length and character check
        if len(color) != 6 or any(c not in string.hexdigits for c in color):
            raise ValueError(f"invalid color code: {value}")

        return str.__new__(cls, color)


# subclass instead of newtype so it exists at runtime, so we can use isinstance
class LocalizedStr(str):
    """Represents a string which has been localized."""

    def __new__(cls, value: str) -> Self:
        # this is a castable type hook but we hint str for usability
        assert isinstance_or_raise(value, str)
        return str.__new__(cls, value)


class LocalizedItem(LocalizedStr):
    pass


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


_T_Sortable = TypeVar("_T_Sortable", bound=Sortable)

_T_covariant = TypeVar("_T_covariant", covariant=True)


def sorted_dict(d: Mapping[_T, _T_Sortable]) -> dict[_T, _T_Sortable]:
    return dict(sorted(d.items(), key=lambda item: item[1]))


class IProperty(Protocol[_T_covariant]):
    def __get__(self, __instance: Any, __owner: type | None = None, /) -> _T_covariant:
        ...
