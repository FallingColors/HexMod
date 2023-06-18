from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Callable, ClassVar, Type, get_args

from common.deserialize import TypedConfig, from_dict_checked
from common.formatting import FormatTree
from common.types import Book, BookHelpers, Entry
from minecraft.i18n import LocalizedStr
from minecraft.resource import ItemStack, ResLoc, ResourceLocation


@dataclass(kw_only=True)
class _BasePage(BookHelpers):
    """Fields shared by all Page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    type: ClassVar[ResourceLocation]
    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: ResourceLocation | None = None

    entry: Entry = field(init=False)
    """This should be initialized by whatever's creating the page (ie. the entry)."""

    def __init_subclass__(cls, type: ResourceLocation) -> None:
        cls.type = type

    @property
    def book(self) -> Book:
        return self.entry.book


@dataclass
class TextPage(_BasePage, type=ResLoc("patchouli", "text")):
    text: FormatTree
    title: LocalizedStr | None = field(default=None, kw_only=True)


@dataclass
class ImagePage(_BasePage, type=ResLoc("patchouli", "image")):
    images: list[ResourceLocation]
    title: LocalizedStr | None = None
    border: bool = False
    text: FormatTree | None = None


@dataclass
class CraftingPage(_BasePage, type=ResLoc("patchouli", "crafting")):
    recipe: ResourceLocation
    recipe2: ResourceLocation | None = None
    title: LocalizedStr | None = None
    text: FormatTree | None = None


@dataclass
class SmeltingPage(_BasePage, type=ResLoc("patchouli", "smelting")):
    recipe: ItemStack
    recipe2: ItemStack | None = None
    title: LocalizedStr | None = None
    text: FormatTree | None = None


@dataclass
class MultiblockPage(_BasePage, type=ResLoc("patchouli", "multiblock")):
    name: LocalizedStr
    multiblock_id: ResourceLocation | None = None
    # TODO: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/multiblocks/
    # this should be a dataclass, but hex doesn't have any multiblock pages so idc
    multiblock: Any | None = None
    enable_visualize: bool = True
    text: FormatTree | None = None

    def __post_init__(self):
        if self.multiblock_id is None and self.multiblock is None:
            raise ValueError(f"One of multiblock_id or multiblock must be set\n{self}")


@dataclass
class EntityPage(_BasePage, type=ResLoc("patchouli", "entity")):
    entity: ItemStack  # TODO: uhh
    scale: float = 1.0
    offset: float | None = None
    rotate: bool = True
    default_rotation: float = -45
    name: LocalizedStr | None = None
    text: FormatTree | None = None


@dataclass
class SpotlightPage(_BasePage, type=ResLoc("patchouli", "spotlight")):
    item: ItemStack
    title: LocalizedStr | None = None
    link_recipe: bool = False
    text: FormatTree | None = None


@dataclass
class LinkPage(TextPage, type=ResLoc("patchouli", "link")):
    url: str
    link_text: LocalizedStr


@dataclass
class RelationsPage(_BasePage, type=ResLoc("patchouli", "relations")):
    entries: list[ResourceLocation]
    title: LocalizedStr = LocalizedStr("Related Chapters")
    text: FormatTree | None = None


@dataclass
class QuestPage(_BasePage, type=ResLoc("patchouli", "quest")):
    trigger: ResourceLocation | None = None
    title: LocalizedStr = LocalizedStr("Objective")
    text: FormatTree | None = None


@dataclass
class EmptyPage(_BasePage, type=ResLoc("patchouli", "empty")):
    draw_filler: bool = True


@dataclass
class HexPatternPage(_BasePage, type=ResLoc("hexcasting", "pattern")):
    op_id: ResourceLocation
    input: str | None = None
    output: str | None = None
    text: FormatTree | None = None

    def __post_init__(self):
        self.name: LocalizedStr = self.i18n.localize_pattern(self.op_id)  # TODO:


@dataclass
class HexManualPatternPage(_BasePage, type=ResLoc("hexcasting")):
    pass


@dataclass
class HexManualPatternNosigPage(_BasePage, type=ResLoc("hexcasting")):
    pass


@dataclass
class HexCraftingMultiPage(_BasePage, type=ResLoc("hexcasting")):
    pass


@dataclass
class HexBrainsweepPage(_BasePage, type=ResLoc("hexcasting")):
    pass


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
    | HexPatternPage
    | HexManualPatternPage
    | HexManualPatternNosigPage
    | HexCraftingMultiPage
    | HexBrainsweepPage
)

_PAGE_TYPES: dict[ResourceLocation, Type[Page]] = {}

for cls in get_args(Page):
    # type check
    if not issubclass(cls, _BasePage):
        raise TypeError(f"Page {cls} must subclass _BasePage, but does not")
    elif cls is _BasePage:
        raise TypeError(f"_BasePage cannot be used as a Page")
    cls: Page = cls  # pylance moment

    # duplicate check
    if other := _PAGE_TYPES.get(cls.type):
        raise ValueError(f"Duplicate page type {cls.type}: {cls}, {other}")

    _PAGE_TYPES[cls.type] = cls


def make_page_hook(config: TypedConfig) -> Callable[[dict[str, Any]], Page]:
    """Creates a type hook for deserializing a Page."""

    def page_hook(raw: dict[str, Any] | str) -> Page:
        # find the dataclass corresponding to the page's type
        if isinstance(raw, str):
            # special case, thanks patchouli
            raw = {"type": "patchouli:text", "text": raw}
            cls = TextPage
        else:
            # get the type
            if (type_ := raw.pop("type")) is None:
                raise ValueError("Invalid page, missing type")
            # get the class
            if (cls := _PAGE_TYPES.get(type_)) is None:
                raise ValueError(f"Unknown page type {type_}")

        # deserialize
        return from_dict_checked(cls, raw, config)

    return page_hook
