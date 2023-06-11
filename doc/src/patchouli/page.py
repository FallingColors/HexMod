from __future__ import annotations

import json
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

from common.abstract import Book, Entry
from common.formatting import FormatTree
from common.pattern_info import PatternInfo, RawPatternInfo

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
# TODO: replace with tagged unions

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

# TODO: what the hell is this
bind1 = (lambda: None).__get__(0).__class__


# TODO: serde
def slurp(filename: str) -> Any:
    with open(filename, "r", encoding="utf-8") as fh:
        return json.load(fh)


def resolve_pattern(book: Book, page: Page_hexcasting_pattern) -> None:
    page["op"] = [book.patterns[page["op_id"]]]
    page["name"] = book.i18n.localize_pattern(page["op_id"])


def fixup_pattern(do_sig: bool, book: Book, page: ManualPatternPage) -> None:
    patterns = page["patterns"]
    if (op_id := page.get("op_id")) is not None:
        page["header"] = book.i18n.localize_pattern(op_id)
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


# TODO: remove
def do_localize(book: Book, obj: Page, *names: str) -> None:
    for name in names:
        if name in obj:
            obj[name] = book.i18n.localize(obj[name])


# TODO: move all of this to the individual page classes
page_transformers: dict[str, Callable[[Book, Any], None]] = {
    "hexcasting:pattern": resolve_pattern,
    "hexcasting:manual_pattern": bind1(fixup_pattern, True),
    "hexcasting:manual_pattern_nosig": bind1(fixup_pattern, False),
    "hexcasting:brainsweep": lambda book, page: page.__setitem__(
        "output_name",
        book.i18n.localize_item(fetch_bswp_recipe_result(book, page["recipe"])),
    ),
    "patchouli:link": lambda book, page: do_localize(book, page, "link_text"),
    "patchouli:crafting": lambda book, page: page.__setitem__(
        "item_name",
        [
            book.i18n.localize_item(fetch_recipe_result(book, page[ty]))
            for ty in ("recipe", "recipe2")
            if ty in page
        ],
    ),
    "hexcasting:crafting_multi": lambda book, page: page.__setitem__(
        "item_name",
        [
            book.i18n.localize_item(fetch_recipe_result(book, recipe))
            for recipe in page["recipes"]
        ],
    ),
    "patchouli:spotlight": lambda book, page: page.__setitem__(
        "item_name", book.i18n.localize_item(page["item"])
    ),
}
