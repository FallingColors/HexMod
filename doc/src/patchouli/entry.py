from __future__ import annotations

import json
from typing import TYPE_CHECKING, Any, Callable, NotRequired, TypedDict

from common.pattern_info import RawPatternInfo
from patchouli.page import (
    ManualPatternPage,
    Page,
    Page_hexcasting_pattern,
    Page_patchouli_text,
)

if TYPE_CHECKING:
    from patchouli.book import Book


class Entry(TypedDict):
    category: str
    icon: str
    id: str
    name: str
    pages: list[Page]
    advancement: NotRequired[str]
    entry_color: NotRequired[str]
    extra_recipe_mappings: NotRequired[dict]
    flag: NotRequired[str]
    priority: NotRequired[bool]
    read_by_default: NotRequired[bool]
    sort_num: NotRequired[int]
    sortnum: NotRequired[float | int]


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
def do_localize(book: Book, obj: Entry | Page, *names: str) -> None:
    for name in names:
        if name in obj:
            obj[name] = book.localize(obj[name])


# TODO: remove
def do_format(book: Book, obj: Entry | Page, *names: str) -> None:
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
