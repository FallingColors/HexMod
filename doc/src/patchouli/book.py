from __future__ import annotations

import re
from dataclasses import InitVar, dataclass, field
from pathlib import Path
from typing import Any, Literal

from common.deserialize import Castable, TypedConfig, TypeHooks, load_json_data, rename
from common.formatting import FormatTree
from common.pattern_info import PatternInfo, PatternStubFile
from common.types import Color, sorted_dict
from dacite import from_dict
from minecraft.i18n import I18n, LocalizedStr
from minecraft.resource import ItemStack, ResourceLocation
from patchouli.category import Category

_DEFAULT_LANG = "en_us"

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

_DEFAULT_PATTERN_RE = re.compile(
    r'HexPattern\.fromAngles\("([qweasd]+)", HexDir\.(\w+)\),\s*modLoc\("([^"]+)"\)([^;]*true\);)?'
)


TextOverflowMode = Literal["overflow"] | Literal["resize"] | Literal["truncate"]


@dataclass
class _BookData:
    """Direct representation of book.json.

    You should probably not use this to edit and re-serialize book.json, because this sets
    all the default values as defined by the docs. (TODO: superclass which doesn't do that)

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    # required
    name: LocalizedStr
    landing_text: FormatTree

    # optional
    book_texture: ResourceLocation = ResourceLocation(
        "patchouli", "textures/gui/book_brown.png"
    )
    filler_texture: ResourceLocation | None = None
    crafting_texture: ResourceLocation | None = None
    model: ResourceLocation = ResourceLocation("patchouli", "book_brown")
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
    i18n: bool = False
    macros: dict[str, str] = field(default_factory=dict)
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
    """Path to the mod's resources directory, eg. `../Common/src/main/resources`."""
    modid: str
    """Mod ID, eg. `hexcasting`."""
    book_name: str
    """Internal Patchouli name for the book, eg. `thehexbook`."""

    pattern_stubs: InitVar[list[PatternStubFile]]
    pattern_re: InitVar[re.Pattern[str]] = _DEFAULT_PATTERN_RE
    lang: InitVar[str] = _DEFAULT_LANG

    def __post_init__(
        self,
        pattern_stubs: list[PatternStubFile],
        pattern_re: re.Pattern[str],
        lang_name: str,
    ) -> None:
        # required init order: i18n/macros, data/patterns, categories

        # standalone non-init fields
        self.blacklist: set[str] = set()
        self.spoilers: set[str] = set()

        # read the raw dict from the json file
        data = load_json_data(_BookData, self.dir / "book.json")

        self.i18n: I18n = I18n(self.resource_dir, self.modid, lang_name, data["i18n"])

        # TODO: order of operations: should default macros really override book macros?
        # this does make a difference - the snapshot tests fail if the order is reversed
        data["macros"].update(_DEFAULT_MACROS)

        # NOW we can create the book dataclass
        config = self.config()
        config.type_hooks[FormatTree] = lambda s: self.format(s, data["macros"])
        self.data: _BookData = from_dict(_BookData, data, config)

        # patterns
        self.patterns: dict[ResourceLocation, PatternInfo] = {}
        for stub in pattern_stubs:
            for pattern in stub.load_patterns(self, pattern_re):
                # check for key clobbering, because why not
                if duplicate := self.patterns.get(pattern.id):
                    raise ValueError(
                        f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}"
                    )
                self.patterns[pattern.id] = pattern

        # categories, sorted by sortnum
        categories = (
            Category.load(path, self) for path in self.categories_dir.rglob("*.json")
        )
        self.categories: dict[ResourceLocation, Category] = {
            category.id: category for category in categories
        }
        self.categories = sorted_dict(self.categories)

    @property
    def dir(self) -> Path:
        """eg. `resources/data/hexcasting/patchouli_books/thehexbook`"""
        return (
            self.resource_dir / "data" / self.modid / "patchouli_books" / self.book_name
        )

    @property
    def categories_dir(self) -> Path:
        return self.dir / _DEFAULT_LANG / "categories"

    @property
    def entries_dir(self) -> Path:
        return self.dir / _DEFAULT_LANG / "entries"

    @property
    def templates_dir(self) -> Path:
        return self.dir / _DEFAULT_LANG / "templates"

    def format(
        self,
        text: str | LocalizedStr,
        macros: dict[str, str] | None = None,
        skip_errors: bool = False,
    ) -> FormatTree:
        """Converts the given string into a FormatTree, localizing it if necessary."""
        if not isinstance(text, LocalizedStr):
            assert isinstance(text, str), f"Expected str, got {type(text)}"
            text = self.i18n.localize(text, skip_errors=skip_errors)

        if macros is None:
            macros = self.data.macros
        return FormatTree.format(macros, text)

    def config(self, type_hooks: TypeHooks = {}) -> TypedConfig:
        """Creates a Dacite config with strict mode and some preset type hooks.

        If passed, type_hooks will be merged with the default hooks. In case of
        conflict, type_hooks will be preferred.
        """
        default_hooks: TypeHooks = {
            ResourceLocation: ResourceLocation.from_str,
            ItemStack: ItemStack.from_str,
            LocalizedStr: self.i18n.localize,
            FormatTree: self.format,
        }
        default_hooks.update(type_hooks)

        return TypedConfig(
            type_hooks=default_hooks,
            cast=[Castable],
            check_types=True,
            strict=True,
            strict_unions_match=True,
        )
