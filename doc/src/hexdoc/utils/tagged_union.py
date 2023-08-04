# pyright: reportPrivateUsage=false

# from __future__ import annotations

from collections import defaultdict
from enum import Enum
from typing import Any, ClassVar, Generator, Self, cast

from pkg_resources import iter_entry_points
from pydantic import ValidationInfo, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from .model import AnyContext, HexDocModel
from .resource import ResourceLocation


class NoValueType(Enum):
    """Type of NoValue, a singleton representing the value of a nonexistent dict key."""

    _token = 0


NoValue = NoValueType._token
"""A singleton (like None) representing the value of a nonexistent dict key."""

TagValue = str | NoValueType

_loaded_groups: set[str] = set()


def load_entry_points(group: str):
    # don't load a group multiple times
    if group in _loaded_groups:
        return
    _loaded_groups.add(group)

    for entry_point in iter_entry_points(group):
        try:
            entry_point.load()
        except ModuleNotFoundError as e:
            e.add_note(
                f'Note: Tried to load entry point "{entry_point}" from {entry_point.dist}'
            )
            raise


class InternallyTaggedUnion(HexDocModel[AnyContext]):
    """Implements [internally tagged unions](https://serde.rs/enum-representations.html#internally-tagged)
    using the [Registry pattern](https://charlesreid1.github.io/python-patterns-the-registry.html).

    To ensure your subtypes are loaded even if they're not imported by any file, add
    the module as a plugin to your package's entry points. For example, to add subtypes
    to a union with `group=foo.bar` using Hatchling, add this to your pyproject.toml:
    ```toml
    [project.entry-points."foo.bar"]
    some-unique-name = "path.to.import.module"
    ```

    Args:
        group: Entry point group for this class. If None, the parent's value is used.
        key: The dict key for the internal tag. If None, the parent's value is used.
        value: The expected tag value for this class. Should be None for types which
            shouldn't be instantiated (eg. abstract classes).
    """

    # inherited
    _group: ClassVar[str | None] = None
    _tag_key: ClassVar[str | None] = None

    # per-class
    __all_subtypes: ClassVar[set[type[Self]]]
    __concrete_subtypes: ClassVar[defaultdict[TagValue, set[type[Self]]]]

    def __init_subclass__(
        cls,
        *,
        group: str | None = None,
        key: str | None = None,
        value: TagValue | None,
    ) -> None:
        # inherited data, so only set if not None
        if group is not None:
            cls._group = group
        if key is not None:
            cls._tag_key = key

        # don't bother with rest of init if it's not part of a union
        if cls._tag_key is None:
            if cls._group is not None:
                raise ValueError(
                    f"Expected cls._group=None for {cls} with key=None, got {cls._group}"
                )
            if value is not None:
                raise ValueError(
                    f"Expected value=None for {cls} with key=None, got {value}"
                )
            return

        # per-class data and lookups
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
        if (tag_key := cls._tag_key) is None:
            raise NotImplementedError
        return tag_key

    @classmethod
    def _supertypes(cls) -> Generator[type[Self], None, None]:
        tag_key = cls._tag_key_or_raise()

        # we consider a type to be its own supertype/subtype
        yield cls

        # recursively yield bases
        # stop when we reach a non-union or a type with a different key (or no key)
        for base in cls.__bases__:
            if issubclass(base, InternallyTaggedUnion) and base._tag_key == tag_key:
                yield base
                yield from base._supertypes()

    @classmethod
    def _all_subtypes(cls):
        return cls.__all_subtypes

    @classmethod
    def _concrete_subtypes(cls):
        return cls.__concrete_subtypes

    @model_validator(mode="wrap")
    @classmethod
    def _resolve_from_dict(
        cls,
        data: dict[str, Any] | Self | Any,
        handler: ModelWrapValidatorHandler[Self],
        info: ValidationInfo,
    ) -> Self:
        # load plugins from entry points
        if cls._group is not None:
            load_entry_points(cls._group)

        # do this early so we know it's part of a union before returning anything
        tag_key = cls._tag_key_or_raise()

        # if it's already instantiated, just return it; otherwise ensure it's a dict
        match data:
            case InternallyTaggedUnion():
                return data
            case dict():
                # ew
                data = cast(dict[str, Any], data)
            case _:
                return handler(data)

        # don't infinite loop calling the same validator forever
        if "__resolved" in data:
            data.pop("__resolved")
            return handler(data)
        data["__resolved"] = True

        # tag value, eg. "minecraft:crafting_shaped"
        tag_value = data.get(tag_key, NoValue)

        # list of matching types, eg. [ShapedCraftingRecipe, ModConditionalShapedCraftingRecipe]
        if (tag_types := cls.__concrete_subtypes.get(tag_value)) is None:
            raise TypeError(f"Unhandled tag: {tag_key}={tag_value} for {cls}: {data}")

        # try all the types
        exceptions: list[Exception] = []
        matches: dict[type[Self], Self] = {}

        context = cast(AnyContext | None, info.context)
        for inner_type in tag_types:
            try:
                matches[inner_type] = inner_type.model_validate(data, context=context)
            except Exception as e:
                exceptions.append(e)

        # ensure we only matched one
        match len(matches):
            case 1:
                return matches.popitem()[1]
            case x if x > 1:
                raise ExceptionGroup(
                    f"Ambiguous union match for {cls} with {cls._tag_key}={tag_value}: {matches.keys()}: {data}",
                    exceptions,
                )
            case _:
                raise ExceptionGroup(
                    f"Failed to match {cls} with {cls._tag_key}={tag_value} to any of {tag_types}: {data}",
                    exceptions,
                )


class TypeTaggedUnion(InternallyTaggedUnion[AnyContext], key="type", value=None):
    type: ResourceLocation | NoValueType | None

    def __init_subclass__(
        cls,
        *,
        group: str | None = None,
        type: TagValue | None,
    ) -> None:
        super().__init_subclass__(group=group, value=type)
        match type:
            case str():
                cls.type = ResourceLocation.from_str(type)
            case _:
                cls.type = type
