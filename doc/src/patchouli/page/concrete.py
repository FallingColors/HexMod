from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from common.deserialize import rename
from common.formatting import FormatTree
from common.types import LocalizedItem, LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import Entity, ItemStack, ResourceLocation

from .abstract import BasePage, PageWithCraftingRecipes, PageWithText, PageWithTitle


@dataclass(kw_only=True)
class TextPage(PageWithTitle, type="patchouli:text"):
    text: FormatTree


@dataclass
class ImagePage(PageWithTitle, type="patchouli:image"):
    images: list[ResourceLocation]
    border: bool = False


@dataclass
class CraftingPage(PageWithCraftingRecipes, type="patchouli:crafting"):
    recipe: CraftingRecipe
    recipe2: CraftingRecipe | None = None

    @property
    def recipes(self) -> list[CraftingRecipe]:
        recipes = [self.recipe]
        if self.recipe2:
            recipes.append(self.recipe2)
        return recipes


# TODO: this should probably inherit PageWithRecipes too
@dataclass
class SmeltingPage(PageWithTitle, type="patchouli:smelting"):
    recipe: ItemStack
    recipe2: ItemStack | None = None


@dataclass
class MultiblockPage(PageWithText, type="patchouli:multiblock"):
    name: LocalizedStr
    multiblock_id: ResourceLocation | None = None
    # TODO: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/multiblocks/
    # this should be a dataclass, but hex doesn't have any multiblock pages so idc
    multiblock: Any | None = None
    enable_visualize: bool = True

    def __post_init__(self):
        if self.multiblock_id is None and self.multiblock is None:
            raise ValueError(f"One of multiblock_id or multiblock must be set\n{self}")


@dataclass
class EntityPage(PageWithText, type="patchouli:entity"):
    entity: Entity
    scale: float = 1
    offset: float = 0
    rotate: bool = True
    default_rotation: float = -45
    name: LocalizedStr | None = None


@dataclass
class SpotlightPage(PageWithTitle, type="patchouli:spotlight"):
    item: LocalizedItem  # TODO: patchi says this is an ItemStack, so this might break
    link_recipe: bool = False


@dataclass
class LinkPage(TextPage, type="patchouli:link"):
    url: str
    link_text: LocalizedStr


@dataclass(kw_only=True)
class RelationsPage(PageWithTitle, type="patchouli:relations"):
    entries: list[ResourceLocation]
    _title: LocalizedStr = field(
        default=LocalizedStr("Related Chapters"), metadata=rename("title")
    )


@dataclass
class QuestPage(PageWithTitle, type="patchouli:quest"):
    trigger: ResourceLocation | None = None
    _title: LocalizedStr = field(
        default=LocalizedStr("Objective"), metadata=rename("title")
    )


@dataclass
class EmptyPage(BasePage, type="patchouli:empty"):
    draw_filler: bool = True
