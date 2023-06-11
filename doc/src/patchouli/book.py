from __future__ import annotations

from dataclasses import InitVar, dataclass
from pathlib import Path
from typing import Literal

from common.deserialize import FromJson
from common.formatting import FormatTree
from common.pattern_info import PatternInfo, PatternStubFile, load_all_patterns
from common.types import Color
from common.utils import sorted_dict
from minecraft.i18n import I18n, LocalizedStr
from minecraft.resource import ItemStack, ResourceLocation
from patchouli.category import Category
from serde import deserialize

_DEFAULT_LANG = "en_us"

_DEFAULT_MACROS: dict[str, str] = {
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


TextOverflowMode = Literal["overflow"] | Literal["resize"] | Literal["truncate"]


@deserialize
class RawBook(FromJson):
    """Direct representation of book.json.

    You should probably not use this to edit and re-serialize book.json, because this sets
    all the default values as defined by the docs. (TODO: superclass which doesn't do that)

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    # required
    name: str
    landing_text: str

    # optional
    book_texture: ResourceLocation = ResourceLocation.field(
        factory="patchouli:textures/gui/book_brown.png"
    )
    filler_texture: ResourceLocation | None = ResourceLocation.field(default=None)
    crafting_texture: ResourceLocation | None = ResourceLocation.field(default=None)
    model: ResourceLocation = ResourceLocation.field(factory="patchouli:book_brown")
    text_color: Color = Color("000000")
    header_color: Color = Color("333333")
    nameplate_color: Color = Color("FFDD00")
    link_color: Color = Color("0000EE")
    link_hover_color: Color = Color("8800EE")
    progress_bar_color: Color = Color("FFFF55")
    progress_bar_background: Color = Color("DDDDDD")
    open_sound: ResourceLocation | None = ResourceLocation.field(default=None)
    flip_sound: ResourceLocation | None = ResourceLocation.field(default=None)
    _index_icon: ResourceLocation | None = ResourceLocation.field(
        rename="index_icon",
        default=None,
    )
    pamphlet: bool = False
    show_progress: bool = True
    version: str | int = 0
    subtitle: str | None = None
    creative_tab: str = "misc"  # TODO: this was changed in 1.19.3+, and again in 1.20
    advancements_tab: str | None = None
    dont_generate_book: bool = False
    custom_book_item: ItemStack | None = ItemStack.field(default=None)
    show_toasts: bool = True
    use_blocky_font: bool = False
    i18n: bool = False
    macros: dict[str, str] | None = None
    pause_game: bool = False
    text_overflow_mode: TextOverflowMode | None = None
    extend: str | None = None
    """NOTE: currently this WILL NOT load values from the target book!"""
    allow_extensions: bool = True

    @property
    def index_icon(self) -> ResourceLocation:
        return self.model if self._index_icon is None else self._index_icon


@dataclass
class Book:
    """Main dataclass for the docgen.

    Includes all data from book.json, all categories, and the pattern lookup.
    """

    resource_dir: Path
    modid: str
    """Mod ID, eg. `hexcasting`."""
    book_name: str
    """Internal Patchouli name for the book, eg. `thehexbook`."""
    pattern_stubs: InitVar[list[PatternStubFile]]
    lang: InitVar[str] = _DEFAULT_LANG

    def __post_init__(
        self,
        pattern_stubs: list[PatternStubFile],
        lang_name: str,
    ) -> None:
        # deserialize raw book
        # must be first
        self.raw: RawBook = RawBook.load(self.dir / "book.json")

        # i18n lookup dict
        # must be initialized before using self.localize or self.format
        self.i18n: I18n = I18n(self.resource_dir, self.modid, lang_name, self.raw.i18n)

        # macros
        # must be initialized before using self.format
        # TODO: order of operations - should default macros really be overriding book macros?
        self.macros: dict[str, str] = {}
        if self.raw.macros is not None:
            self.macros.update(self.raw.macros)
        self.macros.update(_DEFAULT_MACROS)

        # localized strings
        self.name: LocalizedStr = self.i18n.localize(self.raw.name)
        self.landing_text: FormatTree = self.format(self.raw.landing_text)

        # patterns
        # must be initialized before categories
        self.patterns: dict[str, PatternInfo] = load_all_patterns(
            pattern_stubs,
            self.resource_dir,
            self.modid,
        )

        # categories, sorted by sortnum
        self.categories: dict[ResourceLocation, Category] = self._load_categories()
        self.categories = sorted_dict(self.categories)

        # other fields
        self.blacklist: set[str] = set()
        self.spoilers: set[str] = set()

    @property
    def dir(self) -> Path:
        """eg. `resources/data/hexcasting/patchouli_books/thehexbook`"""
        return (
            self.resource_dir / "data" / self.modid / "patchouli_books" / self.book_name
        )

    @property
    def dir_with_lang(self) -> Path:
        """eg. `resources/data/hexcasting/patchouli_books/thehexbook/en_us`"""
        return self.dir / _DEFAULT_LANG

    @property
    def categories_dir(self) -> Path:
        return self.dir_with_lang / "categories"

    @property
    def entries_dir(self) -> Path:
        return self.dir_with_lang / "entries"

    @property
    def templates_dir(self) -> Path:
        return self.dir_with_lang / "templates"

    def format(self, text: str | LocalizedStr, skip_errors: bool = False) -> FormatTree:
        """Converts the given string into a FormatTree, localizing it if necessary."""
        if not isinstance(text, LocalizedStr):
            text = self.i18n.localize(text, skip_errors=skip_errors)
        return FormatTree.format(self.macros, text)

    def _load_categories(self) -> dict[ResourceLocation, Category]:
        """Deserializes and returns a dict of categories in the book.

        Order is implementation-defined, since sorting is not possible until the categories
        have been added to the book.
        """
        categories = (
            Category(path, self) for path in self.categories_dir.rglob("*.json")
        )
        return {category.id: category for category in categories}
