# pyright: reportPrivateUsage=false

from __future__ import annotations

from abc import ABC
from typing import Any, ClassVar, Generator, Type, TypeVar

from dacite.types import extract_generic

from common.dacite_patch import UnionSkip
from common.deserialize import handle_metadata_inplace


class InternallyTaggedUnion(ABC):
    """Implements internally tagged unions.

    tag_name and tag should only be None for base classes.

    See: https://serde.rs/enum-representations.html#internally-tagged
    """

    _tag_name: ClassVar[str | None]
    _tag_value: ClassVar[str | None]

    def __init_subclass__(cls, tag: str | None, value: str | None) -> None:
        cls._tag_name = tag
        cls._tag_value = value

    @classmethod
    def assert_tag(cls, data: dict[str, Any] | Any) -> dict[str, Any]:
        # tag and value should only be None for base classes
        if cls._tag_name is None or cls._tag_value is None:
            raise NotImplementedError

        # this is a type hook, so check the input type
        if not isinstance(data, dict):
            raise TypeError(f"Expected dict, got {type(data)}: {data}")

        # raise if data doesn't have that key or if the value is wrong
        tag_value = data[cls._tag_name]
        if tag_value != cls._tag_value:
            raise UnionSkip(
                f"Expected {cls._tag_name}={cls._tag_value}, got {tag_value}"
            )

        # convenient spot to put it, i guess
        handle_metadata_inplace(cls, data)
        return data


_T = TypeVar("_T")

_T_Union = TypeVar("_T_Union", bound=InternallyTaggedUnion)


def get_union_types(*unions: Type[_T]) -> Generator[Type[_T], None, None]:
    for union in unions:
        yield from extract_generic(union)
