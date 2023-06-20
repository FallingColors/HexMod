from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Callable, Self

from common.deserialize import TypeFn, TypeHooks, rename
from common.formatting import FormatTree
from common.pattern import RawPatternInfo
from common.tagged_union import InternallyTaggedUnion, get_union_types
from common.types import Book, BookHelpers, LocalizedItem, LocalizedStr
from minecraft.recipe import BrainsweepRecipe, CraftingRecipe
from minecraft.resource import Entity, ItemStack, ResourceLocation


@dataclass(kw_only=True)
class BasePage(InternallyTaggedUnion, BookHelpers, tag="type", value=None):
    """Fields shared by all Page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    book: Book

    type: ResourceLocation = field(init=False)
    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    def __init_subclass__(cls, type: str | None) -> None:
        super().__init_subclass__(__class__._tag_name, type)
        if type is not None:
            cls.type = ResourceLocation.from_str(type)

    @classmethod
    def make_type_hook(cls, book: Book) -> TypeFn:
        def type_hook(data: Self | dict[str, Any]) -> Self | dict[str, Any]:
            if isinstance(data, cls):
                return data
            data = cls.assert_tag(data)
            data["book"] = book
            return data

        return type_hook


@dataclass(kw_only=True)
class PageWithText(BasePage, type=None):
    text: FormatTree | None = None


@dataclass(kw_only=True)
class PageWithTitle(PageWithText, type=None):
    _title: LocalizedStr | None = field(default=None, metadata=rename("title"))

    @property
    def title(self):
        return self._title


@dataclass(kw_only=True)
class PageWithCraftingRecipes(PageWithText, ABC, type=None):
    @property
    @abstractmethod
    def recipes(self) -> list[CraftingRecipe]:
        ...


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


@dataclass(kw_only=True)
class PageWithPattern(PageWithTitle, ABC, type=None):
    patterns: list[RawPatternInfo]
    op_id: ResourceLocation | None = None
    header: LocalizedStr | None = None
    input: str | None = None
    output: str | None = None
    hex_size: int | None = None

    _title: None = None

    @classmethod
    def make_type_hook(cls, book: Book) -> Callable[[dict[str, Any]], dict[str, Any]]:
        super_hook = super().make_type_hook(book)

        def type_hook(data: dict[str, Any]) -> dict[str, Any]:
            # convert a single pattern to a list
            data = super_hook(data)
            patterns = data.get("patterns")
            if patterns is not None and not isinstance(patterns, list):
                data["patterns"] = [patterns]
            return data

        return type_hook

    @property
    @abstractmethod
    def name(self) -> LocalizedStr:
        ...

    @property
    def args(self) -> str | None:
        inp = self.input or ""
        oup = self.output or ""
        if inp or oup:
            return f"{inp} \u2192 {oup}".strip()
        return None

    @property
    def title(self) -> LocalizedStr:
        suffix = f" ({self.args})" if self.args else ""
        return LocalizedStr(self.name + suffix)


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
