from __future__ import annotations

import dataclasses as dc
import json
import os
import re
from dataclasses import InitVar, dataclass
from pathlib import Path
from typing import (
    Any,
    Callable,
    Generic,
    Literal,
    LiteralString,
    NotRequired,
    TypedDict,
    TypeVar,
)

from common.deserialize import FromJson, load_i18n
from common.formatting import FormatTree
from common.pattern_info import (
    PatternInfo,
    PatternStubFile,
    RawPatternInfo,
    load_patterns,
)
from common.types import Color, ItemStack, LocalizedStr, ResourceLocation
from serde import deserialize, field, serde

_T_LiteralString = TypeVar("_T_LiteralString", bound=LiteralString)


class _BasePage(TypedDict, Generic[_T_LiteralString]):
    type: _T_LiteralString


class Page_patchouli_text(_BasePage[Literal["patchouli:text"]]):
    text: FormatTree | list
    anchor: NotRequired[str]
    input: NotRequired[str]
    op_id: NotRequired[str]
    output: NotRequired[str]
    title: NotRequired[str]


class Page_patchouli_link(_BasePage[Literal["patchouli:link"]]):
    link_text: str
    text: FormatTree
    url: str


class Page_patchouli_spotlight(_BasePage[Literal["patchouli:spotlight"]]):
    item: str
    item_name: str
    link_recipe: bool
    text: FormatTree
    anchor: NotRequired[str]


class Page_patchouli_crafting(_BasePage[Literal["patchouli:crafting"]]):
    item_name: list
    recipe: str
    anchor: NotRequired[str]
    recipe2: NotRequired[str]
    text: NotRequired[FormatTree | list]
    title: NotRequired[str]


class Page_patchouli_image(_BasePage[Literal["patchouli:image"]]):
    border: bool
    images: list
    title: str


class Page_patchouli_empty(_BasePage[Literal["patchouli:empty"]]):
    pass


class Page_hexcasting_pattern(_BasePage[Literal["hexcasting:pattern"]]):
    name: str
    op: list[PatternInfo]
    op_id: str
    text: FormatTree | list
    anchor: NotRequired[str]
    header: NotRequired[str]
    hex_size: NotRequired[int]
    input: NotRequired[str]
    output: NotRequired[str]


class Page_hexcasting_manual_pattern(_BasePage[Literal["hexcasting:manual_pattern"]]):
    anchor: str
    header: str
    op: list[RawPatternInfo]
    patterns: dict | list
    text: FormatTree
    input: NotRequired[str]
    op_id: NotRequired[str]
    output: NotRequired[str]


class Page_hexcasting_manual_pattern_nosig(
    _BasePage[Literal["hexcasting:manual_pattern_nosig"]]
):
    header: str
    op: list[RawPatternInfo]
    patterns: dict | list
    text: FormatTree


class Page_hexcasting_crafting_multi(_BasePage[Literal["hexcasting:crafting_multi"]]):
    heading: str
    item_name: list
    recipes: list
    text: FormatTree


class Page_hexcasting_brainsweep(_BasePage[Literal["hexcasting:brainsweep"]]):
    output_name: str
    recipe: str
    text: FormatTree


# convenient type aliases
# TODO: replace with polymorphism, probably

Page = (
    Page_patchouli_text
    | Page_patchouli_link
    | Page_patchouli_spotlight
    | Page_patchouli_crafting
    | Page_patchouli_image
    | Page_patchouli_empty
    | Page_hexcasting_pattern
    | Page_hexcasting_manual_pattern
    | Page_hexcasting_manual_pattern_nosig
    | Page_hexcasting_crafting_multi
    | Page_hexcasting_brainsweep
)

RecipePage = (
    Page_patchouli_crafting
    | Page_hexcasting_crafting_multi
    | Page_hexcasting_brainsweep
)

PatternPageWithSig = Page_hexcasting_pattern | Page_hexcasting_manual_pattern

ManualPatternPage = (
    Page_hexcasting_manual_pattern | Page_hexcasting_manual_pattern_nosig
)

PatternPage = (
    Page_hexcasting_pattern
    | Page_hexcasting_manual_pattern
    | Page_hexcasting_manual_pattern_nosig
)


class Entry(TypedDict):
    category: str
    icon: str
    id: str
    name: str
    pages: list[_BasePage]
    advancement: NotRequired[str]
    entry_color: NotRequired[str]
    extra_recipe_mappings: NotRequired[dict]
    flag: NotRequired[str]
    priority: NotRequired[bool]
    read_by_default: NotRequired[bool]
    sort_num: NotRequired[int]
    sortnum: NotRequired[float | int]


class Category(TypedDict):
    description: FormatTree
    entries: list[Entry]
    icon: str
    id: str
    name: str
    sortnum: int
    entry_color: NotRequired[str]
    flag: NotRequired[str]
    parent: NotRequired[str]


# TODO: what the hell is this
bind1 = (lambda: None).__get__(0).__class__


# TODO: serde
def slurp(filename: str) -> Any:
    with open(filename, "r", encoding="utf-8") as fh:
        return json.load(fh)


def resolve_pattern(book: Book, page: Page_hexcasting_pattern) -> None:
    page["op"] = [book.patterns[page["op_id"]]]
    page["name"] = book.localize_pattern(page["op_id"])


def fixup_pattern(do_sig: bool, book: Book, page: ManualPatternPage) -> None:
    patterns = page["patterns"]
    if (op_id := page.get("op_id")) is not None:
        page["header"] = book.localize_pattern(op_id)
    if not isinstance(patterns, list):
        patterns = [patterns]
    if do_sig:
        inp = page.get("input", None) or ""
        oup = page.get("output", None) or ""
        pipe = f"{inp} \u2192 {oup}".strip()
        suffix = f" ({pipe})" if inp or oup else ""
        page["header"] += suffix
    page["op"] = [
        RawPatternInfo(p["startdir"], p["signature"], False) for p in patterns
    ]


# TODO: recipe type (not a page, apparently)
def fetch_recipe(book: Book, recipe: str) -> dict[str, dict[str, str]]:
    modid, recipeid = recipe.split(":")
    gen_resource_dir = (
        book.resource_dir.as_posix()
        .replace("/main/", "/generated/")
        .replace("Common/", "Forge/")
    )  # TODO hack
    recipe_path = f"{gen_resource_dir}/data/{modid}/recipes/{recipeid}.json"
    return slurp(recipe_path)


def fetch_recipe_result(book: Book, recipe: str):
    return fetch_recipe(book, recipe)["result"]["item"]


def fetch_bswp_recipe_result(book: Book, recipe: str):
    return fetch_recipe(book, recipe)["result"]["name"]


# TODO: move all of this to the individual page classes
page_transformers: dict[str, Callable[[Book, Any], None]] = {
    "hexcasting:pattern": resolve_pattern,
    "hexcasting:manual_pattern": bind1(fixup_pattern, True),
    "hexcasting:manual_pattern_nosig": bind1(fixup_pattern, False),
    "hexcasting:brainsweep": lambda book, page: page.__setitem__(
        "output_name",
        book.localize_item(fetch_bswp_recipe_result(book, page["recipe"])),
    ),
    "patchouli:link": lambda book, page: do_localize(book, page, "link_text"),
    "patchouli:crafting": lambda book, page: page.__setitem__(
        "item_name",
        [
            book.localize_item(fetch_recipe_result(book, page[ty]))
            for ty in ("recipe", "recipe2")
            if ty in page
        ],
    ),
    "hexcasting:crafting_multi": lambda book, page: page.__setitem__(
        "item_name",
        [
            book.localize_item(fetch_recipe_result(book, recipe))
            for recipe in page["recipes"]
        ],
    ),
    "patchouli:spotlight": lambda book, page: page.__setitem__(
        "item_name", book.localize_item(page["item"])
    ),
}

# TODO: remove
def do_localize(book: Book, obj: Category | Entry | Page, *names: str) -> None:
    for name in names:
        if name in obj:
            obj[name] = book.localize(obj[name])


# TODO: remove
def do_format(book: Book, obj: Category | Entry | Page, *names: str) -> None:
    for name in names:
        if name in obj:
            obj[name] = book.format(obj[name])


# TODO: move to serde
def parse_entry(book: Book, entry_path: str, ent_name: str) -> Entry:
    data: Entry = slurp(f"{entry_path}")
    do_localize(book, data, "name")
    for i, page in enumerate(data["pages"]):
        if isinstance(page, str):
            page = Page_patchouli_text(type="patchouli:text", text=book.format(page))
            data["pages"][i] = page
        else:
            do_format(book, page, "text")
        do_localize(book, page, "title", "header")
        if page_transformer := page_transformers.get(page["type"]):
            page_transformer(book, page)
    data["id"] = ent_name

    return data


def parse_category(book: Book, base_dir: str, cat_name: str) -> Category:
    data: Category = slurp(f"{base_dir}/categories/{cat_name}.json")
    do_localize(book, data, "name")
    do_format(book, data, "description")

    entry_dir = f"{base_dir}/entries/{cat_name}"
    entries: list[Entry] = []
    for filename in os.listdir(entry_dir):
        if filename.endswith(".json"):
            basename = filename[:-5]
            entries.append(
                parse_entry(book, f"{entry_dir}/{filename}", cat_name + "/" + basename)
            )
    entries.sort(
        key=lambda ent: (
            not ent.get("priority", False),
            ent.get("sortnum", 0),
            ent["name"],
        )
    )
    data["entries"] = entries
    data["id"] = cat_name

    return data


def parse_sortnum(cats: dict[str, Category], name: str) -> tuple[int, ...]:
    if "/" in name:
        ix = name.rindex("/")
        return parse_sortnum(cats, name[:ix]) + (cats[name].get("sortnum", 0),)
    return (cats[name].get("sortnum", 0),)


_DEFAULT_LANG_NAME = "en_us"

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
        "patchouli:textures/gui/book_brown.png"
    )
    filler_texture: ResourceLocation | None = ResourceLocation.field(default=None)
    crafting_texture: ResourceLocation | None = ResourceLocation.field(default=None)
    model: ResourceLocation = ResourceLocation.field("patchouli:book_brown")
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
        rename="index_icon", default=None
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
    Includes all data from book.json, some useful paths, and the pattern lookup.

    Constructor opens a bunch of files.
    """

    # constructor args
    resource_dir: Path
    modid: str
    patchouli_name: str
    pattern_stubs: InitVar[list[PatternStubFile]]
    lang_name: InitVar[str] = _DEFAULT_LANG_NAME

    def __post_init__(
        self,
        pattern_stubs: list[PatternStubFile],
        lang_name: str,
    ) -> None:
        # deserialize raw book
        # must be initialized first
        self.raw: RawBook = RawBook.load(self.book_dir / "book.json")

        # lang
        # must be initialized before using self.localize or self.format
        lang_file = self.lang_dir / f"{lang_name}.json"
        self.lang: dict[str, LocalizedStr] | None = (
            load_i18n(lang_file) if self.raw.i18n else None
        )

        # macros
        # must be initialized before using self.format
        # TODO: order of operations - should default macros really be overriding book macros?
        self.macros: dict[str, str] = {}
        if self.raw.macros is not None:
            self.macros.update(self.raw.macros)
        self.macros.update(_DEFAULT_MACROS)

        # other fields
        self.blacklist: set[str] = set()
        self.spoilers: set[str] = set()

        # localized strings
        self.name: LocalizedStr = self.localize(self.raw.name)
        self.landing_text: FormatTree = self.format(self.raw.landing_text)

        # patterns
        self.patterns: dict[str, PatternInfo] = load_patterns(
            pattern_stubs,
            self.resource_dir,
            self.modid,
        )

        # categories
        # TODO: make this not awful
        self.categories: list[Category] = []
        base_dir = self.book_dir / _DEFAULT_LANG_NAME
        categories_dir = base_dir / "categories"
        for path in categories_dir.rglob("*.json"):
            basename = path.relative_to(categories_dir).with_suffix("").as_posix()
            self.categories.append(parse_category(self, base_dir.as_posix(), basename))
        cats = {cat["id"]: cat for cat in self.categories}
        self.categories.sort(
            key=lambda cat: (parse_sortnum(cats, cat["id"]), cat["name"])
        )

    @property
    def book_dir(self) -> Path:
        return (
            self.resource_dir
            / "data"
            / self.modid
            / "patchouli_books"
            / self.patchouli_name
        )

    @property
    def lang_dir(self) -> Path:
        return self.resource_dir / "assets" / self.modid / "lang"

    def localize(
        self,
        key: str,
        default: str | None = None,
        skip_errors: bool = False,
    ) -> LocalizedStr:
        """Looks up the given string in the lang table if i18n is enabled.
        Otherwise, returns the original key.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        if self.lang is None:
            return LocalizedStr(key.replace("%%", "%"))

        if default is not None:
            localized = self.lang.get(key, default)
        elif skip_errors:
            localized = self.lang.get(key, key)
        else:
            # raises if not found
            localized = self.lang[key]

        return LocalizedStr(localized.replace("%%", "%"))

    def localize_pattern(self, op_id: str, skip_errors: bool = False) -> LocalizedStr:
        """Localizes the given pattern id (internal name, eg. brainsweep).

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        try:
            # prefer the book-specific translation if it exists
            # don't pass skip_errors here because we need to catch it below
            return self.localize(f"hexcasting.spell.book.{op_id}")
        except KeyError:
            return self.localize(f"hexcasting.spell.{op_id}", skip_errors=skip_errors)

    def localize_item(self, item: str, skip_errors: bool = False) -> LocalizedStr:
        """Localizes the given item resource name.

        Raises KeyError if i18n is enabled and skip_errors is False but the key has no localization.
        """
        # FIXME: hack
        item = re.sub(r"{.*", "", item.replace(":", "."))
        try:
            return self.localize(f"block.{item}")
        except KeyError:
            return self.localize(f"item.{item}", skip_errors=skip_errors)

    def format(self, text: str | LocalizedStr, skip_errors: bool = False) -> FormatTree:
        """Converts the given string into a FormatTree, localizing it if necessary."""
        if not isinstance(text, LocalizedStr):
            text = self.localize(text, skip_errors=skip_errors)
        return FormatTree.format(self.macros, text)
