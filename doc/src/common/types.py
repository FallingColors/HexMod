from __future__ import annotations

import string
from abc import ABC, abstractmethod
from typing import Any, Mapping, Protocol, TypeVar

from pydantic import field_validator, model_validator
from pydantic.dataclasses import dataclass

from common.model import DEFAULT_CONFIG

_T = TypeVar("_T")


@dataclass(config=DEFAULT_CONFIG, frozen=True)
class Color:
    """Represents a hexadecimal color.

    Inputs are coerced to lowercase `rrggbb`. Raises ValueError on invalid input.

    Valid formats, all of which would be converted to `0099ff`:
    - `"#0099FF"`
    - `"#0099ff"`
    - `"#09F"`
    - `"#09f"`
    - `"0099FF"`
    - `"0099ff"`
    - `"09F"`
    - `"09f"`
    - `0x0099ff`
    """

    value: str

    @model_validator(mode="before")
    def _pre_root(cls, value: Any):
        if isinstance(value, (str, int)):
            return {"value": value}
        return value

    @field_validator("value", mode="before")
    def _check_value(cls, value: str | int | Any) -> str:
        # type check
        match value:
            case str():
                value = value.removeprefix("#").lower()
            case int():
                # int to hex string
                value = f"{value:0>6x}"
            case _:
                raise TypeError(f"Expected str or int, got {type(value)}")

        # 012 -> 001122
        if len(value) == 3:
            value = "".join(c + c for c in value)

        # length and character check
        if len(value) != 6 or any(c not in string.hexdigits for c in value):
            raise ValueError(f"invalid color code: {value}")

        return value


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
