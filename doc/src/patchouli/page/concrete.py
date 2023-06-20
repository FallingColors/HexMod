from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from common.deserialize import TypeHooks, rename
from common.formatting import FormatTree
from common.pattern import RawPatternInfo
from common.tagged_union import get_union_types
from common.types import Book, LocalizedItem, LocalizedStr
from minecraft.recipe import BrainsweepRecipe, CraftingRecipe
from minecraft.resource import Entity, ItemStack, ResourceLocation

from .abstract import (
    BasePage,
    PageWithCraftingRecipes,
    PageWithPattern,
    PageWithText,
    PageWithTitle,
)


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


@dataclass
class PatternPage(PageWithPattern, type="hexcasting:pattern"):
    patterns: list[RawPatternInfo] = field(init=False)
    op_id: ResourceLocation
    header: None

    def __post_init__(self):
        self.patterns = [self.book.patterns[self.op_id]]

    @property
    def name(self) -> LocalizedStr:
        return self.i18n.localize_pattern(self.op_id)


@dataclass
class ManualPatternNosigPage(PageWithPattern, type="hexcasting:manual_pattern_nosig"):
    header: LocalizedStr
    op_id: None
    input: None
    output: None

    @property
    def name(self) -> LocalizedStr:
        return self.header


@dataclass
class ManualOpPatternPage(PageWithPattern, type="hexcasting:manual_pattern"):
    op_id: ResourceLocation
    header: None

    @property
    def name(self) -> LocalizedStr:
        return self.i18n.localize_pattern(self.op_id)


@dataclass
class ManualRawPatternPage(PageWithPattern, type="hexcasting:manual_pattern"):
    op_id: None
    header: LocalizedStr

    @property
    def name(self) -> LocalizedStr:
        return self.header


@dataclass
class CraftingMultiPage(PageWithCraftingRecipes, type="hexcasting:crafting_multi"):
    heading: LocalizedStr  # ...heading?
    _recipes: list[CraftingRecipe] = field(metadata=rename("recipes"))

    @property
    def recipes(self) -> list[CraftingRecipe]:
        return self._recipes


@dataclass
class BrainsweepPage(PageWithText, type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe


Page = (
    TextPage
    | ImagePage
    | CraftingPage
    | SmeltingPage
    | MultiblockPage
    | EntityPage
    | SpotlightPage
    | LinkPage
    | RelationsPage
    | QuestPage
    | EmptyPage
    | PatternPage
    | ManualPatternNosigPage
    | ManualOpPatternPage
    | ManualRawPatternPage
    | CraftingMultiPage
    | BrainsweepPage
)


def _raw_page_hook(data: dict[str, Any] | str) -> dict[str, Any]:
    if isinstance(data, str):
        # special case, thanks patchouli
        return {"type": "patchouli:text", "text": data}
    return data


def make_page_hooks(book: Book) -> TypeHooks:
    """Creates type hooks for deserializing Page types."""

    type_hooks: TypeHooks = {Page: _raw_page_hook}

    for cls_ in get_union_types(Page):
        type_hooks[cls_] = cls_.make_type_hook(book)

    return type_hooks
