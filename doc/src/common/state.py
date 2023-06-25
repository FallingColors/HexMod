from __future__ import annotations

from dataclasses import InitVar, dataclass
from itertools import chain
from pathlib import Path
from re import sub
from typing import Any, Collection, Generic, Iterable, Mapping, Self, Type, TypeVar

from common.deserialize import (
    TypedConfig,
    TypeHook,
    TypeHooks,
    from_dict_checked,
    load_json_data,
)
from common.formatting import DEFAULT_MACROS, FormatTree
from common.pattern import Direction
from common.properties import Properties
from common.tagged_union import InternallyTaggedUnion
from common.types import LocalizedItem, LocalizedStr, isinstance_or_raise
from minecraft.i18n import I18n
from minecraft.resource import ItemStack, ResourceLocation


@dataclass
class BookState:
    """Stores data which needs to be accessible/mutable from many different places.

    This helps us avoid some *really* ugly circular dependencies in the book tree.
    """

    props: Properties
    i18n: I18n
    macros: InitVar[dict[str, str]]
    """Extra formatting macros to be merged with the provided defaults.
    
    These should generally come from `book.json`.
    """
    type_hooks: InitVar[TypeHooks[Any] | None] = None
    """Extra Dacite type hooks to be merged with the provided defaults.

    This should only be used if necessary to avoid a circular dependency. In general,
    you should add hooks by subclassing BookState and adding them in __post_init__ after
    calling super.
    
    Hooks are added in the following order. In case of conflict, later values will
    override earlier ones.
    * `state._default_hooks()`
    * `type_hooks`
    * `type_hook_maker`
    """
    # oh my god
    stateful_unions: InitVar[StatefulUnions[Self] | None] = None

    def __post_init__(
        self,
        macros: dict[str, str],
        type_hooks: TypeHooks[Any] | None,
        stateful_unions: StatefulUnions[Self] | None,
    ):
        # macros (TODO: order of operations?)
        self._macros: dict[str, str] = macros | DEFAULT_MACROS

        # type conversion hooks
        self._type_hooks: TypeHooks[Any] = {
            ResourceLocation: ResourceLocation.from_str,
            ItemStack: ItemStack.from_str,
            Direction: Direction.__getitem__,
            LocalizedStr: self.i18n.localize,
            LocalizedItem: self.i18n.localize_item,
            FormatTree: self.format,
        }
        if type_hooks:
            self._type_hooks |= type_hooks
        if stateful_unions:
            for base, subtypes in stateful_unions.items():
                self._type_hooks |= make_stateful_union_hooks(base, subtypes, self)

    def format(self, text: str | LocalizedStr) -> FormatTree:
        """Converts the given string into a FormatTree, localizing it if necessary."""
        # we use this as a type hook
        assert isinstance_or_raise(text, (str, LocalizedStr))

        if not isinstance(text, LocalizedStr):
            text = self.i18n.localize(text)
        return FormatTree.format(self._macros, text)

    @property
    def config(self) -> TypedConfig:
        """Creates a Dacite config."""
        return TypedConfig(type_hooks=self._type_hooks)


AnyState = TypeVar("AnyState", bound=BookState)


@dataclass(kw_only=True)
class Stateful(Generic[AnyState]):
    """Base for dataclasses with a BookState object.

    Provides some helper properties to make the state more ergonomic to use.
    """

    state: AnyState

    @property
    def props(self):
        return self.state.props

    @property
    def i18n(self):
        return self.state.i18n


@dataclass(kw_only=True)
class StatefulFile(Stateful[AnyState]):
    """Base for dataclasses which can be loaded from a JSON file given a path and the
    shared state. Extends Stateful."""

    path: Path

    @classmethod
    def load(cls, path: Path, state: AnyState) -> Self:
        # load the raw data from json, and add our extra fields
        data = load_json_data(cls, path, {"path": path, "state": state})
        return from_dict_checked(cls, data, state.config, path)


class StatefulInternallyTaggedUnion(
    Stateful[AnyState],
    InternallyTaggedUnion,
    tag=None,
    value=None,
):
    @classmethod
    def resolve_union_with_state(
        cls,
        data: Self | dict[str, Any] | Any,
        state: AnyState,
    ) -> Self | dict[str, Any]:
        if isinstance(data, dict):
            data["state"] = state
        return cls.resolve_union(data, state.config)

    @classmethod
    def make_type_hook(cls, state: AnyState) -> TypeHook[Self]:
        return lambda data: cls.resolve_union_with_state(data, state)


StatefulUnions = Mapping[
    Type[StatefulInternallyTaggedUnion[AnyState]],
    Collection[Type[StatefulInternallyTaggedUnion[AnyState]]],
]


def make_stateful_union_hooks(
    base: Type[StatefulInternallyTaggedUnion[AnyState]],
    subtypes: Iterable[Type[StatefulInternallyTaggedUnion[AnyState]]],
    state: AnyState,
) -> TypeHooks[StatefulInternallyTaggedUnion[AnyState]]:
    return {
        subtype: subtype.make_type_hook(state) for subtype in chain([base], subtypes)
    }
