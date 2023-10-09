from typing import Any, Self

from pydantic import model_validator

from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.assets.textures import ItemWithTexture
from hexdoc.minecraft.recipe import CraftingRecipe
from hexdoc.utils import Entity, ItemStack, ResourceLocation

from ..text import FormatTree
from .abstract_pages import Page, PageWithText, PageWithTitle


class TextPage(PageWithTitle, type="patchouli:text"):
    text: FormatTree


class ImagePage(PageWithTitle, type="patchouli:image"):
    images: list[ResourceLocation]
    border: bool = False


class CraftingPage(PageWithTitle, type="patchouli:crafting"):
    recipe: CraftingRecipe
    recipe2: CraftingRecipe | None = None

    @property
    def recipes(self) -> list[CraftingRecipe]:
        return [r for r in [self.recipe, self.recipe2] if r is not None]


class SmeltingPage(PageWithTitle, type="patchouli:smelting"):
    recipe: ItemStack
    recipe2: ItemStack | None = None


class MultiblockPage(PageWithText, type="patchouli:multiblock"):
    name: LocalizedStr
    multiblock_id: ResourceLocation | None = None
    # TODO: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/multiblocks/
    # this should be a modeled class, but hex doesn't have any multiblock pages so idc
    multiblock: Any | None = None
    enable_visualize: bool = True

    @model_validator(mode="after")
    def _check_multiblock(self) -> Self:
        if self.multiblock_id is None and self.multiblock is None:
            raise ValueError(f"One of multiblock_id or multiblock must be set\n{self}")
        return self


class EntityPage(PageWithText, type="patchouli:entity"):
    entity: Entity
    scale: float = 1
    offset: float = 0
    rotate: bool = True
    default_rotation: float = -45
    name: LocalizedStr | None = None


class SpotlightPage(PageWithTitle, type="patchouli:spotlight"):
    item: ItemWithTexture
    link_recipe: bool = False


class LinkPage(TextPage, type="patchouli:link"):
    url: str
    link_text: LocalizedStr


class RelationsPage(PageWithTitle, type="patchouli:relations"):
    entries: list[ResourceLocation]
    title: LocalizedStr = LocalizedStr.with_value("Related Chapters")


class QuestPage(PageWithTitle, type="patchouli:quest"):
    trigger: ResourceLocation | None = None
    title: LocalizedStr = LocalizedStr.with_value("Objective")


class EmptyPage(
    Page,
    type="patchouli:empty",
    template_type="patchouli:page",
):
    draw_filler: bool = True
