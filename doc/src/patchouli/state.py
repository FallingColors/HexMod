from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Generic, Self, TypeVar

from common.deserialize import (
    TypedConfig,
    TypeHook,
    TypeHooks,
    from_dict_checked,
    load_json_data,
)
from common.pattern import Direction
from common.properties import Properties
from common.tagged_union import InternallyTaggedUnion
from common.types import LocalizedItem, LocalizedStr, isinstance_or_raise
from minecraft.i18n import I18n
from minecraft.resource import ItemStack, ResourceLocation

from .formatting import DEFAULT_MACROS, FormatTree


@dataclass
class BookState:
    """Stores data which needs to be accessible/mutable from many different places.

    This helps us avoid some *really* ugly circular dependencies in the book tree.
    """

    props: Properties

    def __post_init__(self):
        self._macros: dict[str, str] = DEFAULT_MACROS
        self._i18n: I18n | None = None

        # type conversion hooks
        self._type_hooks: TypeHooks[Any] = {
            ResourceLocation: ResourceLocation.from_str,
            ItemStack: ItemStack.from_str,
            Direction: Direction.__getitem__,
            FormatTree: self.format,
        }

    @property
    def i18n(self) -> I18n:
        if self._i18n is None:
            raise RuntimeError("Tried to use state.i18n before initializing it")
        return self._i18n

    @i18n.setter
    def i18n(self, i18n: I18n):
        self._i18n = i18n
        self._type_hooks |= {
            LocalizedStr: self.i18n.localize,
            LocalizedItem: self.i18n.localize_item,
        }

    def add_macros(self, macros: dict[str, str]):
        # TODO: order of operations?
        self._macros = macros | self._macros

    def add_stateful_unions(
        self,
        *unions: type[StatefulInternallyTaggedUnion[Self]],
    ):
        for union in unions:
            self._type_hooks |= union.make_type_hooks(self)

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


_T = TypeVar("_T", dict[str, Any], Any)


class StatefulInternallyTaggedUnion(
    Stateful[AnyState],
    InternallyTaggedUnion,
    key=None,
    value=None,
):
    @classmethod
    def _with_state(cls, data: _T, state: AnyState) -> _T:
        if isinstance(data, dict):
            return data | {"state": state}
        return data

    @classmethod
    def make_type_hook(cls, state: AnyState) -> TypeHook[Self]:
        def type_hook(data: Self | Any):
            data = cls._with_state(data, state)
            return cls._resolve_from_dict(data, state.config)

        return type_hook

    @classmethod
    def make_type_hooks(cls, state: BookState) -> TypeHooks[Self]:
        return {
            subtype: subtype.make_type_hook(state) for subtype in cls._all_subtypes()
        }


@dataclass(kw_only=True)
class TypeTaggedUnion(StatefulInternallyTaggedUnion[AnyState], key="type", value=None):
    type: ResourceLocation = field(init=False)

    def __init_subclass__(cls, type: str | None) -> None:
        super().__init_subclass__("type", type)
        if type is not None:
            cls.type = ResourceLocation.from_str(type)

    @property
    def _tag_value(self) -> str:
        return str(self.type)
