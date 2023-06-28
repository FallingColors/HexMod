from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Self, TypeVar

from common.deserialize import rename
from common.types import LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import ResourceLocation

from ..formatting import FormatTree
from ..state import AnyState, StatefulTypeTaggedUnion

_T = TypeVar("_T")


@dataclass(kw_only=True)
class Page(StatefulTypeTaggedUnion[AnyState], group="hexdoc.Page", type=None):
    """Base class for Patchouli page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    @classmethod
    def stateful_type_hook(cls, data: Self | Any, state: AnyState) -> Self:
        if isinstance(data, str):
            data = {"type": "patchouli:text", "text": data}
        return super().stateful_type_hook(data, state)


@dataclass(kw_only=True)
class PageWithText(Page[AnyState], type=None):
    text: FormatTree | None = None


@dataclass(kw_only=True)
class PageWithTitle(PageWithText[AnyState], type=None):
    _title: LocalizedStr | None = field(default=None, metadata=rename("title"))

    @property
    def title(self) -> LocalizedStr | None:
        return self._title


@dataclass(kw_only=True)
class PageWithCraftingRecipes(PageWithText[AnyState], ABC, type=None):
    @property
    @abstractmethod
    def recipes(self) -> list[CraftingRecipe]:
        ...
