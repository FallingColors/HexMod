from collections import defaultdict
from enum import Enum
from typing import Any, ClassVar, Generator, Self

import more_itertools
from pydantic import ValidationInfo, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.plugin.manager import PluginManagerContext
from hexdoc.utils.deserialize import cast_or_raise

from .model import HexdocModel
from .resource import ResourceLocation


class NoValueType(Enum):
    """Type of NoValue, a singleton representing the value of a nonexistent dict key."""

    _token = 0


NoValue = NoValueType._token  # pyright: ignore[reportPrivateUsage]
"""A singleton (like None) representing the value of a nonexistent dict key."""

TagValue = str | NoValueType

# sentinel value to check if we already loaded the tagged union subtypes hook
_is_loaded = False  # pyright: ignore[reportPrivateUsage]


class InternallyTaggedUnion(HexdocModel):
    """Implements [internally tagged unions](https://serde.rs/enum-representations.html#internally-tagged)
    using the [Registry pattern](https://charlesreid1.github.io/python-patterns-the-registry.html).

    To ensure your subtypes are loaded even if they're not imported by any file, add a
    Pluggy hook implementation for `hexdoc_load_tagged_unions() -> list[Package]`.

    Subclasses MUST NOT be generic unless they provide a default value for all
    `__init_subclass__` arguments. See pydantic/7171 for more info.

    Args:
        key: The dict key for the internal tag. If None, the parent's value is used.
        value: The expected tag value for this class. Should be None for types which
            shouldn't be instantiated (eg. abstract classes).
    """

    # inherited
    _tag_key: ClassVar[str | None] = None

    # per-class
    __all_subtypes: ClassVar[set[type[Self]]]
    __concrete_subtypes: ClassVar[defaultdict[TagValue, set[type[Self]]]]

    def __init_subclass__(
        cls,
        *,
        key: str | None = None,
        value: TagValue | None,
    ) -> None:
        # inherited data, so only set if not None
        if key is not None:
            cls._tag_key = key

        # don't bother with rest of init if it's not part of a union
        if cls._tag_key is None:
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
        value: Any,
        handler: ModelWrapValidatorHandler[Self],
        info: ValidationInfo,
    ) -> Self:
        context = cast_or_raise(info.context, PluginManagerContext)

        # load plugins from entry points
        global _is_loaded
        if not _is_loaded:
            more_itertools.consume(context.pm.load_tagged_unions())
            _is_loaded = True

        # do this early so we know it's part of a union before returning anything
        tag_key = cls._tag_key_or_raise()

        # if it's already instantiated, just return it; otherwise ensure it's a dict
        match value:
            case InternallyTaggedUnion():
                return value
            case dict():
                data: dict[str, Any] = value
            case _:
                return handler(value)

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
                message = f"Ambiguous union match for {cls} with {cls._tag_key}={tag_value}: {matches.keys()}: {data}"
                if exceptions:
                    raise ExceptionGroup(message, exceptions)
                raise RuntimeError(message)
            case _:
                message = f"Failed to match {cls} with {cls._tag_key}={tag_value} to any of {tag_types}: {data}"
                if exceptions:
                    raise ExceptionGroup(message, exceptions)
                raise RuntimeError(message)


class TypeTaggedUnion(InternallyTaggedUnion, key="type", value=None):
    type: ResourceLocation | NoValueType | None

    def __init_subclass__(cls, *, type: TagValue | None) -> None:
        super().__init_subclass__(value=type)
        match type:
            case str():
                cls.type = ResourceLocation.from_str(type)
            case _:
                cls.type = type
