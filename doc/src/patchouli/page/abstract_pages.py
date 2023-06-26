from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Self

from common.deserialize import TypeHook, rename
from common.formatting import FormatTree
from common.types import LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import ResourceLocation
from patchouli.state import AnyState, TypeTaggedUnion


@dataclass(kw_only=True)
class Page(TypeTaggedUnion[AnyState], type=None):
    """Base class for Patchouli page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    @classmethod
    def make_type_hook(cls, state: AnyState) -> TypeHook[Self]:
        super_hook = super().make_type_hook(state)

        def type_hook(data: Self | Any) -> Self | dict[str, Any]:
            # special case, thanks patchouli
            if isinstance(data, str):
                data = {"type": "patchouli:text", "text": data}
            return super_hook(data)

        return type_hook


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
    def recipes(self) -> list[CraftingRecipe[AnyState]]:
        ...
