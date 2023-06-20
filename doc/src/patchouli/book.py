from __future__ import annotations

from abc import ABC
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Literal, Self

import patchouli
from common.deserialize import (
    TypedConfig,
    TypeHooks,
    from_dict_checked,
    load_json_data,
    rename,
)
from common.formatting import FormatTree
from common.pattern import Direction
from common.properties import Properties
from common.tagged_union import get_union_types
from common.types import Color, IProperty, LocalizedItem, LocalizedStr, sorted_dict
from minecraft.i18n import I18n
from minecraft.recipe import Recipe
from minecraft.resource import ItemStack, ResLoc, ResourceLocation

_DEFAULT_MACROS = {
    "$(obf)": "$(k)",
    "$(bold)": "$(l)",
    "$(strike)": "$(m)",
    "$(italic)": "$(o)",
    "$(italics)": "$(o)",
    "$(list": "$(li",
    "$(reset)": "$()",
    "$(clear)": "$()",
    "$(2br)": "$(br2)",
    "$(p)": "$(br2)",
    "/$": "$()",
    "<br>": "$(br)",
    "$(nocolor)": "$(0)",
    "$(item)": "$(#b0b)",
    "$(thing)": "$(#490)",
}

_DEFAULT_TYPE_HOOKS: TypeHooks = {
    ResourceLocation: ResourceLocation.from_str,
    ItemStack: ItemStack.from_str,
    Direction: Direction.__getitem__,
}


def _format(text: str | LocalizedStr, i18n: I18n, macros: dict[str, str]):
    if not isinstance(text, LocalizedStr):
        assert isinstance(text, str), f"Expected str, got {type(text)}: {text}"
        text = i18n.localize(text)
    return FormatTree.format(macros, text)


@dataclass
class Book:
    """Main Patchouli book class.

    Includes all data from book.json, categories/entries/pages, and i18n.

    You should probably not use this to edit and re-serialize book.json, because this sets
    all the default values as defined by the docs. (TODO: superclass which doesn't do that)

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    props: Properties
    i18n: I18n

    # required from book.json
    name: LocalizedStr
    landing_text: FormatTree

    # optional from book.json
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

    @property
    def index_icon(self) -> ResourceLocation:
        # default value as defined by patchouli, apparently
        return self.model if self._index_icon is None else self._index_icon

    @classmethod
    def load(cls, props: Properties) -> Self:
        """Sets up i18n, macros, and the book.json data."""

        # read the raw dict from the json file
        path = props.book_dir / "book.json"
        data = load_json_data(cls, path)

        data["props"] = props

        i18n = I18n(props, data["do_i18n"])
        data["i18n"] = i18n

        # TODO: order of operations: should default macros really override book macros?
        # this does make a difference - the snapshot tests fail if the order is reversed
        data["macros"].update(_DEFAULT_MACROS)

        # NOW we can convert the actual book data
        initial_type_hooks: TypeHooks = {
            LocalizedStr: i18n.localize,
            FormatTree: lambda s: _format(s, i18n, data["macros"]),
        }
        config = TypedConfig(type_hooks=_DEFAULT_TYPE_HOOKS | initial_type_hooks)
        return from_dict_checked(cls, data, config, path)

    def __post_init__(self, *args: Any, **kwargs: Any) -> None:
        # type hooks which need a Book instance
        # Dacite fails to type-check TypeHooks, so this CANNOT be a dataclass field
        self.type_hooks: TypeHooks = _DEFAULT_TYPE_HOOKS | {
            LocalizedStr: self.i18n.localize,
            LocalizedItem: self.i18n.localize_item,
            FormatTree: self.format,
            **patchouli.page.make_page_hooks(self),
            **{cls: cls.make_type_hook(self) for cls in get_union_types(Recipe)},
        }

        # best name ever tbh
        self.__post_init_pre_categories__(*args, **kwargs)

        # categories
        self.categories: dict[ResourceLocation, patchouli.Category] = {}
        for path in self.categories_dir.rglob("*.json"):
            category = patchouli.Category.load(path, self)
            self.categories[category.id] = category

        # NOTE: category sorting requires book.categories to already contain all of the
        # categories, because category sorting depends on its parent, and categories get
        # their parent from the book. it's mildly scuffed, but it works
        self.categories = sorted_dict(self.categories)

        # TODO: entries

    def __post_init_pre_categories__(self) -> None:
        """Subclasses may override this method to run code just before categories are
        loaded and deserialized.

        Type hooks are initialized before this, so you can add more here if needed.
        """

    @property
    def categories_dir(self) -> Path:
        return self.props.book_dir / self.props.i18n.lang / "categories"

    @property
    def entries_dir(self) -> Path:
        return self.props.book_dir / self.props.i18n.lang / "entries"

    @property
    def templates_dir(self) -> Path:
        return self.props.book_dir / self.props.i18n.lang / "templates"

    def format(
        self,
        text: str | LocalizedStr,
        skip_errors: bool = False,
    ) -> FormatTree:
        """Converts the given string into a FormatTree, localizing it if necessary."""
        return _format(text, self.i18n, self.macros)

    def config(self, extra_type_hooks: TypeHooks = {}) -> TypedConfig:
        """Creates a Dacite config with strict mode and some preset type hooks.

        If passed, extra_type_hooks will be merged with the default hooks. In case of
        conflict, extra_type_hooks will be preferred.
        """
        return TypedConfig(type_hooks={**self.type_hooks, **extra_type_hooks})


class BookHelpers(ABC):
    """Shortcuts for types with a book field."""

    book: Book | IProperty[Book]

    @property
    def props(self):
        return self.book.props

    @property
    def i18n(self):
        return self.book.i18n
