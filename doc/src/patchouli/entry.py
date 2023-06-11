from __future__ import annotations

from typing import TYPE_CHECKING, NotRequired, TypedDict

from patchouli.page import Page, Page_patchouli_text, page_transformers, slurp

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
