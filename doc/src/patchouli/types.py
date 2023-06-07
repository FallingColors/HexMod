# these are mostly just copied from HexBug for now
# TODO: use dataclasses-json

from typing import (
    Generic,
    Literal,
    LiteralString,
    NamedTuple,
    NotRequired,
    Self,
    TypedDict,
    TypeVar,
)

T = TypeVar("T", bound=LiteralString)


class Style(NamedTuple):
    type: str
    value: str | bool | dict[str, str] | None


class FormatTree(NamedTuple):
    style: Style
    children: list[str | Self]


class BookPage(TypedDict, Generic[T]):
    type: T


class BookPage_patchouli_text(BookPage[Literal["patchouli:text"]]):
    text: FormatTree | list
    anchor: NotRequired[str]
    input: NotRequired[str]
    op_id: NotRequired[str]
    output: NotRequired[str]
    title: NotRequired[str]


class BookPage_hexcasting_manual_pattern_nosig(
    BookPage[Literal["hexcasting:manual_pattern_nosig"]]
):
    header: str
    op: list
    patterns: dict | list
    text: FormatTree


class BookPage_patchouli_link(BookPage[Literal["patchouli:link"]]):
    link_text: str
    text: FormatTree
    url: str


class BookPage_patchouli_spotlight(BookPage[Literal["patchouli:spotlight"]]):
    item: str
    item_name: str
    link_recipe: bool
    text: FormatTree
    anchor: NotRequired[str]


class BookPage_hexcasting_crafting_multi(
    BookPage[Literal["hexcasting:crafting_multi"]]
):
    heading: str
    item_name: list
    recipes: list
    text: FormatTree


class BookPage_patchouli_crafting(BookPage[Literal["patchouli:crafting"]]):
    item_name: list
    recipe: str
    anchor: NotRequired[str]
    recipe2: NotRequired[str]
    text: NotRequired[FormatTree | list]
    title: NotRequired[str]


class BookPage_hexcasting_brainsweep(BookPage[Literal["hexcasting:brainsweep"]]):
    output_name: str
    recipe: str
    text: FormatTree


class BookPage_patchouli_image(BookPage[Literal["patchouli:image"]]):
    border: bool
    images: list
    title: str


class BookPage_hexcasting_pattern(BookPage[Literal["hexcasting:pattern"]]):
    name: str
    op: list
    op_id: str
    text: FormatTree | list
    anchor: NotRequired[str]
    header: NotRequired[str]
    hex_size: NotRequired[int]
    input: NotRequired[str]
    output: NotRequired[str]


class BookPage_hexcasting_manual_pattern(
    BookPage[Literal["hexcasting:manual_pattern"]]
):
    anchor: str
    header: str
    op: list
    patterns: dict | list
    text: FormatTree
    input: NotRequired[str]
    op_id: NotRequired[str]
    output: NotRequired[str]


class BookPage_patchouli_empty(BookPage[Literal["patchouli:empty"]]):
    pass


class BookPage_hexal_everbook_entry(BookPage[Literal["hexal:everbook_entry"]]):
    pass


class BookEntry(TypedDict):
    category: str
    icon: str
    id: str
    name: str
    pages: list[BookPage]
    advancement: NotRequired[str]
    entry_color: NotRequired[str]
    extra_recipe_mappings: NotRequired[dict]
    flag: NotRequired[str]
    priority: NotRequired[bool]
    read_by_default: NotRequired[bool]
    sort_num: NotRequired[int]
    sortnum: NotRequired[float | int]


class BookCategory(TypedDict):
    description: FormatTree | list
    entries: list[BookEntry]
    icon: str
    id: str
    name: str
    sortnum: int
    entry_color: NotRequired[str]
    flag: NotRequired[str]
    parent: NotRequired[str]


class Book(TypedDict):
    blacklist: set
    categories: list[BookCategory]
    i18n: dict[str, str]
    landing_text: FormatTree
    macros: dict
    modid: str
    name: str
    pattern_reg: dict
    resource_dir: str
    spoilers: set
    version: int
    book_texture: NotRequired[str]
    creative_tab: NotRequired[str]
    extend: NotRequired[str]
    filler_texture: NotRequired[str]
    model: NotRequired[str]
    nameplate_color: NotRequired[str]
    show_progress: NotRequired[bool]
    src_dir: NotRequired[str]
