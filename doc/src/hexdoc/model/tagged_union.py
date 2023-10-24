# pyright: reportPrivateUsage=false

from collections import defaultdict
from textwrap import dedent
from typing import Any, ClassVar, Generator, Self, Unpack

import more_itertools
from pydantic import ConfigDict, ValidationInfo, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.core.resource import ResourceLocation
from hexdoc.plugin.manager import PluginManagerContext
from hexdoc.utils.classproperty import classproperty
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.singletons import Inherit, InheritType, NoValue, NoValueType

from .base import HexdocModel

TagValue = str | NoValueType

_RESOLVED = "__resolved"

# sentinel value to check if we already loaded the tagged union subtypes hook
_is_loaded = False


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
    _tag_value: ClassVar[TagValue | None] = None

    # per-class
    __all_subtypes: ClassVar[set[type[Self]]]
    __concrete_subtypes: ClassVar[defaultdict[TagValue, set[type[Self]]]]

    def __init_subclass__(
        cls,
        *,
        key: str | InheritType | None = Inherit,
        value: TagValue | InheritType | None = Inherit,
        **kwargs: Unpack[ConfigDict],
    ):
        super().__init_subclass__(**kwargs)

        # inherited data
        if key is not Inherit:
            cls._tag_key = key
        if value is not Inherit:
            cls._tag_value = value

        # don't bother with rest of init if it's not part of a union
        if cls._tag_key is None:
            if cls._tag_value is None:
                return
            raise ValueError(
                f"Expected value=None for {cls} with key=None, got {value}"
            )

        # per-class data and lookups
        cls.__all_subtypes = set()
        cls.__concrete_subtypes = defaultdict(set)

        # add to all the parents
        for supertype in cls._supertypes():
            supertype.__all_subtypes.add(cls)
            if cls._tag_value is not None:
                supertype.__concrete_subtypes[cls._tag_value].add(cls)

    @classmethod
    def _tag_key_or_raise(cls) -> str:
        if cls._tag_key is None:
            raise NotImplementedError
        return cls._tag_key

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
            case dict() if _RESOLVED not in value:
                data: dict[str, Any] = value
                data[_RESOLVED] = True
            case _:
                return handler(value)

        # tag value, eg. "minecraft:crafting_shaped"
        tag_value = data.get(tag_key, NoValue)

        # list of matching types, eg. [ShapedCraftingRecipe, ModConditionalShapedCraftingRecipe]
        tag_types = cls.__concrete_subtypes.get(tag_value)
        if tag_types is None:
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
                ambiguous_types = ", ".join(str(t) for t in matches.keys())
                reason = f"Ambiguous union match: {ambiguous_types}"
            case _:
                reason = "No match found"

        # something went wrong, raise an exception
        message = dedent(
            f"""\
            Failed to match tagged union {cls}: {reason}
            Tag: {cls._tag_key}={tag_value}
            Types: {", ".join(str(t) for t in tag_types)}
            Data: {data}"""
        )
        if exceptions:
            raise ExceptionGroup(message, exceptions)
        raise RuntimeError(message)

    @model_validator(mode="before")
    def _pop_temporary_keys(cls, value: dict[Any, Any] | Any):
        if isinstance(value, dict) and _RESOLVED in value:
            # copy because this validator may be called multiple times
            # eg. two types with the same key
            value = value.copy()
            value.pop(_RESOLVED)
            assert value.pop(cls._tag_key, NoValue) == cls._tag_value
        return value


class TypeTaggedUnion(InternallyTaggedUnion, key="type", value=None):
    _type: ClassVar[ResourceLocation | NoValueType | None] = None

    def __init_subclass__(
        cls,
        *,
        type: TagValue | InheritType | None,
        **kwargs: Unpack[ConfigDict],
    ):
        super().__init_subclass__(value=type, **kwargs)

        match cls._tag_value:
            case str(raw_value):
                cls._type = ResourceLocation.from_str(raw_value)
            case value:
                cls._type = value

    @classproperty
    @classmethod
    def type(cls):
        return cls._type
