# pyright: reportPrivateUsage=false

from __future__ import annotations

from abc import ABC, abstractmethod
from collections import defaultdict
from itertools import chain
from typing import Any, ClassVar, Iterable, Self, Type, TypeVar

from dacite import StrictUnionMatchError, UnionMatchError, from_dict

from common.dacite_patch import UnionSkip
from common.deserialize import TypedConfig, TypeHooks
from common.types import isinstance_or_raise


class WrongTag(UnionSkip):
    def __init__(self, union_type: Type[InternallyTaggedUnion], tag_value: str) -> None:
        super().__init__(
            f"Expected {union_type._tag_key}={union_type._expected_tag_value}, got {tag_value}"
        )


class InternallyTaggedUnion(ABC):
    """Implements internally tagged unions.

    tag_name and tag should only be None for base classes.

    See: https://serde.rs/enum-representations.html#internally-tagged
    """

    _tag_key: ClassVar[str | None] = None
    _expected_tag_value: ClassVar[str | None] = None
    _all_union_types: ClassVar[list[Type[Self]]]
    _concrete_union_types: ClassVar[defaultdict[str, list[Type[Self]]]]

    def __init_subclass__(cls, tag: str | None, value: str | None) -> None:
        cls._tag_key = tag
        cls._expected_tag_value = value
        cls._all_union_types = []
        cls._concrete_union_types = defaultdict(list)

        # if cls is a concrete union type, add it to all the lookups of its parents
        # also add it to its own lookup so it can resolve itself
        if tag is not None:
            for base in [cls] + cls._union_bases():
                base._all_union_types.append(cls)
                if value is not None:
                    base._concrete_union_types[value].append(cls)

    @classmethod
    def _union_bases(cls) -> list[Type[InternallyTaggedUnion]]:
        union_bases: list[Type[InternallyTaggedUnion]] = []
        for base in cls.__bases__:
            if (
                issubclass(base, InternallyTaggedUnion)
                and base._tag_key is not None
                and base._tag_key == cls._tag_key
            ):
                union_bases += [base] + base._union_bases()
        return union_bases

    @classmethod
    def resolve_union(cls, data: Self | Any, config: TypedConfig) -> Self:
        if cls._tag_key is None:
            raise NotImplementedError

        # if it's already instantiated, just return it; otherwise ensure it's a dict
        if isinstance(data, InternallyTaggedUnion):
            assert isinstance_or_raise(data, cls)
            return data
        assert isinstance_or_raise(data, dict[str, Any])

        # get the class objects for this type
        tag_value = data.get(cls._tag_key)
        if tag_value is None:
            raise KeyError(cls._tag_key, data)

        tag_types = cls._concrete_union_types.get(tag_value)
        if tag_types is None:
            raise TypeError(
                f"Unhandled tag: {cls._tag_key}={tag_value} for {cls}: {data}"
            )

        # try all the types
        exceptions: list[Exception] = []
        union_matches: dict[Type[InternallyTaggedUnion], InternallyTaggedUnion] = {}

        for inner_type in tag_types:
            try:
                value = from_dict(inner_type, data, config)
                if not config.strict_unions_match:
                    return value
                union_matches[inner_type] = value
            except UnionSkip:
                pass
            except Exception as e:
                exceptions.append(e)

        # ensure we only matched one
        match len(union_matches):
            case 1:
                return union_matches.popitem()[1]
            case x if x > 1 and config.strict_unions_match:
                exceptions.append(StrictUnionMatchError(union_matches))
            case _:
                exceptions.append(UnionMatchError(tag_types, data))

        # oopsies
        raise ExceptionGroup(
            f"Failed to match {cls} with {cls._tag_key}={tag_value} to any of {tag_types}: {data}",
            exceptions,
        )

    @property
    @abstractmethod
    def _tag_value(self) -> str:
        ...


_T_Union = TypeVar("_T_Union", bound=InternallyTaggedUnion)


def make_internally_tagged_hooks(
    base: Type[_T_Union],
    subtypes: Iterable[Type[_T_Union]],
    config: TypedConfig,
) -> TypeHooks[_T_Union]:
    """Creates type hooks for an internally tagged union."""
    return {
        subtype: lambda data: subtype.resolve_union(data, config)
        for subtype in chain([base], subtypes)
    }
