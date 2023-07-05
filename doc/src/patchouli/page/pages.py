from typing import Any

from pydantic import Field

from minecraft.i18n import LocalizedItem, LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import Entity, ItemStack, ResourceLocation
from patchouli.context import BookContext

from ..text import FormatTree
from .abstract_pages import Page, PageWithCraftingRecipes, PageWithText, PageWithTitle


class TextPage(PageWithTitle[BookContext], type="patchouli:text"):
    text: FormatTree


class ImagePage(PageWithTitle[BookContext], type="patchouli:image"):
    images: list[ResourceLocation]
    border: bool = False


class CraftingPage(
    PageWithCraftingRecipes[BookContext],
    type="patchouli:crafting",
):
    recipe: CraftingRecipe
    recipe2: CraftingRecipe | None = None

    @property
    def recipes(self) -> list[CraftingRecipe]:
        recipes = [self.recipe]
        if self.recipe2:
            recipes.append(self.recipe2)
        return recipes


# TODO: this should probably inherit PageWithRecipes too
class SmeltingPage(PageWithTitle[BookContext], type="patchouli:smelting"):
    recipe: ItemStack
    recipe2: ItemStack | None = None


class MultiblockPage(PageWithText[BookContext], type="patchouli:multiblock"):
    name: LocalizedStr
    multiblock_id: ResourceLocation | None = None
    # TODO: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/multiblocks/
    # this should be a modeled class, but hex doesn't have any multiblock pages so idc
    multiblock: Any | None = None
    enable_visualize: bool = True

    def __post_init__(self):
        if self.multiblock_id is None and self.multiblock is None:
            raise ValueError(f"One of multiblock_id or multiblock must be set\n{self}")


class EntityPage(PageWithText[BookContext], type="patchouli:entity"):
    entity: Entity
    scale: float = 1
    offset: float = 0
    rotate: bool = True
    default_rotation: float = -45
    name: LocalizedStr | None = None


class SpotlightPage(PageWithTitle[BookContext], type="patchouli:spotlight"):
    item: LocalizedItem  # TODO: patchi says this is an ItemStack, so this might break
    link_recipe: bool = False


class LinkPage(TextPage, type="patchouli:link"):
    url: str
    link_text: LocalizedStr


class RelationsPage(PageWithTitle[BookContext], type="patchouli:relations"):
    entries: list[ResourceLocation]
    title_: LocalizedStr = Field(
        default=LocalizedStr.skip_key("Related Chapters"),
        alias="title",
    )


class QuestPage(PageWithTitle[BookContext], type="patchouli:quest"):
    trigger: ResourceLocation | None = None
    title_: LocalizedStr = Field(
        default=LocalizedStr.skip_key("Objective"),
        alias="title",
    )


class EmptyPage(Page[BookContext], type="patchouli:empty"):
    draw_filler: bool = True
