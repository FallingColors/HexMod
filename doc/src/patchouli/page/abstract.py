from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Self

from common.deserialize import TypeHook, rename
from common.formatting import FormatTree
from common.state import AnyState, StatefulInternallyTaggedUnion
from common.types import LocalizedStr
from minecraft.recipe.concrete import CraftingRecipe
from minecraft.resource import ResourceLocation


@dataclass(kw_only=True)
class Page(StatefulInternallyTaggedUnion[AnyState], tag="type", value=None):
    """Base class for Patchouli page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    type: ResourceLocation = field(init=False)
    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    def __init_subclass__(cls, type: str | None) -> None:
        super().__init_subclass__("type", type)
        if type is not None:
            cls.type = ResourceLocation.from_str(type)

    @classmethod
    def make_type_hook(cls, state: AnyState) -> TypeHook[Self]:
        super_hook = super().make_type_hook(state)

        def type_hook(data: Self | Any) -> Self | dict[str, Any]:
            # special case, thanks patchouli
            if isinstance(data, str):
                data = {"type": "patchouli:text", "text": data}
            return super_hook(data)

        return type_hook

    @property
    def _tag_value(self) -> str:
        return str(self.type)


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
