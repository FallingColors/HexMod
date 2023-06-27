# pyright: reportPrivateUsage=false

from __future__ import annotations

from abc import ABC, abstractmethod
from collections import defaultdict
from typing import Any, ClassVar, Generator, Self

from dacite import StrictUnionMatchError, UnionMatchError, from_dict

from common.dacite_patch import UnionSkip
from common.deserialize import TypedConfig
from common.types import isinstance_or_raise


class NoValueType(object):
    """Represents a dict key which is expected to not exist."""

    def __repr__(self) -> str:
        return "NoValue"


NoValue = NoValueType()

TagValue = str | NoValueType


class WrongTagSkip(UnionSkip):
    def __init__(self, union_type: type[InternallyTaggedUnion], tag_value: str) -> None:
        super().__init__(
            f"Expected {union_type.__tag_key}={union_type.__expected_tag_value}, got {tag_value}"
        )


class InternallyTaggedUnion(ABC):
    """Implements [internally tagged unions](https://serde.rs/enum-representations.html#internally-tagged)
    using the [Registry pattern](https://charlesreid1.github.io/python-patterns-the-registry.html).

    NOTE: Make sure the module where you declare your union types is actually imported,
    or they won't be registered.

    Args:
        key: The dict key for the internal tag. Should be None for classes which are not
            part of a union.
        value: The expected tag value for this class. Should be None for types which
            shouldn't be instantiated (eg. abstract classes).
    """

    __tag_key: ClassVar[str | None] = None
    __expected_tag_value: ClassVar[TagValue | None]
    __all_subtypes: ClassVar[set[type[Self]]]
    __concrete_subtypes: ClassVar[defaultdict[TagValue, set[type[Self]]]]

    def __init_subclass__(cls, key: str | None, value: TagValue | None) -> None:
        # don't bother initializing classes which aren't part of any union
        if key is None:
            if value is not None:
                raise ValueError(
                    f"Expected value=None for {cls} with key=None, got {value}"
                )
            return

        # set up per-class data and lookups
        cls.__tag_key = key
        cls.__expected_tag_value = value
        cls.__all_subtypes = set()
        cls.__concrete_subtypes = defaultdict(set)

        # add to all the parents
        is_concrete = value is not None
        for supertype in cls._supertypes():
            supertype.__all_subtypes.add(cls)
            if is_concrete:
                supertype.__concrete_subtypes[value].add(cls)

    @classmethod
    def _tag_key_or_raise(cls) -> str:
        if (tag_key := cls.__tag_key) is None:
            raise NotImplementedError
        return tag_key

    @classmethod
    def _supertypes(cls) -> Generator[type[InternallyTaggedUnion], None, None]:
        tag_key = cls._tag_key_or_raise()

        # we consider a type to be its own supertype/subtype
        yield cls

        # recursively yield bases
        # stop when we reach a non-union or a type with a different key (or no key)
        for base in cls.__bases__:
            if issubclass(base, InternallyTaggedUnion) and base.__tag_key == tag_key:
                yield base
                yield from base._supertypes()

    @classmethod
    def _all_subtypes(cls):
        return cls.__all_subtypes

    @classmethod
    def _concrete_subtypes(cls):
        return cls.__concrete_subtypes

    @classmethod
    def _resolve_from_dict(cls, data: Self | Any, config: TypedConfig) -> Self:
        # do this first so we know it's part of a union
        tag_key = cls._tag_key_or_raise()

        # if it's already instantiated, just return it; otherwise ensure it's a dict
        if isinstance(data, InternallyTaggedUnion):
            assert isinstance_or_raise(data, cls)
            return data
        assert isinstance_or_raise(data, dict[str, Any])

        # tag value, eg. "minecraft:crafting_shaped"
        tag_value = data.get(tag_key, NoValue)

        # list of matching types, eg. [ShapedCraftingRecipe, ModConditionalShapedCraftingRecipe]
        if (tag_types := cls.__concrete_subtypes.get(tag_value)) is None:
            raise TypeError(f"Unhandled tag: {tag_key}={tag_value} for {cls}: {data}")

        # try all the types
        exceptions: list[Exception] = []
        union_matches: dict[type[InternallyTaggedUnion], InternallyTaggedUnion] = {}

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
            f"Failed to match {cls} with {cls.__tag_key}={tag_value} to any of {tag_types}: {data}",
            exceptions,
        )

    @property
    @abstractmethod
    def _tag_value(self) -> str:
        ...
