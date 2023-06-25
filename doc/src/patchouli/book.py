from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Literal, Self, Type

from common.deserialize import TypeHooks, from_dict_checked, load_json_data, rename
from common.formatting import FormatTree
from common.properties import Properties
from common.state import AnyState, BookState, Stateful, StatefulUnions
from common.types import Color, LocalizedStr
from minecraft.i18n import I18n
from minecraft.recipe import Recipe
from minecraft.recipe.concrete import CraftingShapedRecipe, CraftingShapelessRecipe
from minecraft.resource import ItemStack, ResLoc, ResourceLocation
from patchouli.page.concrete import (
    CraftingPage,
    EmptyPage,
    EntityPage,
    ImagePage,
    LinkPage,
    MultiblockPage,
    QuestPage,
    RelationsPage,
    SmeltingPage,
    SpotlightPage,
    TextPage,
)

from .category import Category
from .entry import Entry
from .page import Page

_PAGES: list[Type[Page[BookState]]] = [
    TextPage,
    ImagePage,
    CraftingPage,
    SmeltingPage,
    MultiblockPage,
    EntityPage,
    SpotlightPage,
    LinkPage,
    RelationsPage,
    QuestPage,
    EmptyPage,
]

_RECIPES: list[Type[Recipe[BookState]]] = [
    CraftingShapedRecipe,
    CraftingShapelessRecipe,
]


@dataclass
class Book(Stateful[AnyState], ABC):
    """Main Patchouli book class.

    Includes all data from book.json, categories/entries/pages, and i18n.

    You should probably not use this (or any other Patchouli types, eg. Category, Entry)
    to edit and re-serialize book.json, because this class sets all the default values
    as defined by the docs. (TODO: superclass which doesn't do that)

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    # required
    name: LocalizedStr
    landing_text: FormatTree

    # optional
    book_texture: ResourceLocation = ResLoc("patchouli", "textures/gui/book_brown.png")
    filler_texture: ResourceLocation | None = None
    crafting_texture: ResourceLocation | None = None
    model: ResourceLocation = ResLoc("patchouli", "book_brown")
    text_color: Color = Color("000000")
    header_color: Color = Color("333333")
    nameplate_color: Color = Color("FFDD00")
    link_color: Color = Color("0000EE")
    link_hover_color: Color = Color("8800EE")
    progress_bar_color: Color = Color("FFFF55")
    progress_bar_background: Color = Color("DDDDDD")
    open_sound: ResourceLocation | None = None
    flip_sound: ResourceLocation | None = None
    _index_icon: ResourceLocation | None = field(
        default=None, metadata=rename("index_icon")
    )
    pamphlet: bool = False
    show_progress: bool = True
    version: str | int = 0
    subtitle: LocalizedStr | None = None
    creative_tab: str = "misc"  # TODO: this was changed in 1.19.3+, and again in 1.20
    advancements_tab: str | None = None
    dont_generate_book: bool = False
    custom_book_item: ItemStack | None = None
    show_toasts: bool = True
    use_blocky_font: bool = False
    do_i18n: bool = field(default=False, metadata=rename("i18n"))
    macros: dict[str, str] = field(default_factory=dict)
    pause_game: bool = False
    text_overflow_mode: Literal["overflow", "resize", "truncate"] | None = None
    extend: str | None = None
    """NOTE: currently this WILL NOT load values from the target book!"""
    allow_extensions: bool = True

    @classmethod
    def load(cls, props: Properties) -> Self:
        """Loads `book.json`, and sets up shared state with `cls._make_state()`.

        Subclasses should generally not override this. To customize state creation or
        add type hooks (including page or recipe types), override `_init_state()`.
        """

        # read the raw dict from the json file
        path = props.book_dir / "book.json"
        data = load_json_data(cls, path)

        # construct the shared state object
        state = data["state"] = cls._init_state(
            data=data,
            props=props,
            i18n=I18n(props, data["do_i18n"]),
            macros=data["macros"],
            type_hooks={},
            stateful_unions={
                Page[BookState]: _PAGES,
                Recipe[BookState]: _RECIPES,
            },
        )

        # NOW we can convert the actual book data
        return from_dict_checked(cls, data, state.config, path)

    @classmethod
    @abstractmethod
    def _init_state(
        cls,
        data: dict[str, Any],
        props: Properties,
        i18n: I18n,
        macros: dict[str, str],
        type_hooks: TypeHooks[Any],
        stateful_unions: StatefulUnions[AnyState],
    ) -> AnyState:
        """Constructs the shared state object for this book.

        `macros` should not include `DEFAULT_MACROS`; those are added by the BookState
        constructor.

        You can add Page or Recipe types to `stateful_unions`."""
        ...

    def __post_init__(self) -> None:
        """Loads categories and entries."""
        # categories
        self.categories = Category.load_all(self.state)

        # entries
        for path in self.props.entries_dir.rglob("*.json"):
            # i used the entry to insert the entry (pretty sure thanos said that)
            entry = Entry.load(path, self.state)
            self.categories[entry.category_id].entries.append(entry)

        # we inserted a bunch of entries in no particular order, so sort each category
        for category in self.categories.values():
            category.entries.sort()

    @property
    def index_icon(self) -> ResourceLocation:
        # default value as defined by patchouli, apparently
        return self.model if self._index_icon is None else self._index_icon
