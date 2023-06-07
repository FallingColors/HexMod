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
    children: list[Self | str]


Text = FormatTree | str


class _BasePage(TypedDict, Generic[T]):
    type: T


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
    op: list
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
    op: list
    patterns: dict | list
    text: FormatTree
    input: NotRequired[str]
    op_id: NotRequired[str]
    output: NotRequired[str]


class Page_hexcasting_manual_pattern_nosig(
    _BasePage[Literal["hexcasting:manual_pattern_nosig"]]
):
    header: str
    op: list
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
    description: FormatTree | list
    entries: list[Entry]
    icon: str
    id: str
    name: str
    sortnum: int
    entry_color: NotRequired[str]
    flag: NotRequired[str]
    parent: NotRequired[str]


# TODO: class
Registry = dict[str, tuple[str, str, bool]]


class Book(TypedDict):
    blacklist: set
    categories: list[Category]
    i18n: dict[str, str]
    landing_text: FormatTree
    macros: dict[str, str]
    modid: str
    name: str
    pattern_reg: Registry
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
