from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any

from common.composition import Book, Category, WithBook
from common.deserialize import FromJson
from common.utils import Sortable
from minecraft.i18n import LocalizedStr
from minecraft.resource import ItemStack, ResourceLocation, WithPathId
from patchouli.page import Page, Page_patchouli_text, page_transformers
from serde import deserialize


# TODO: remove
def do_localize(book: Book, obj: Page | dict[str, Any], *names: str) -> None:
    for name in names:
        if name in obj:
            obj[name] = book.i18n.localize(obj[name])


# TODO: remove
def do_format(book: Book, obj: Page | dict[str, Any], *names: str) -> None:
    for name in names:
        if name in obj:
            obj[name] = book.format(obj[name])


@deserialize
class RawEntry(FromJson):
    """Direct representation of an Entry json file.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/entry-json
    """

    # required
    name: str
    category: ResourceLocation = ResourceLocation.field()
    icon: ItemStack = ItemStack.field()
    pages: list[dict[str, Any] | str]  # TODO: type

    # optional
    advancement: ResourceLocation | None = ResourceLocation.field(default=None)
    flag: str | None = None
    priority: bool = False
    secret: bool = False
    read_by_default: bool = False
    sortnum: int = 0
    turnin: ResourceLocation | None = ResourceLocation.field(default=None)
    # TODO: this should be dict[ItemStack, int] but I have no idea how to make that work
    extra_recipe_mappings: dict[str, int] | None = None


@dataclass
class Entry(WithBook, WithPathId, Sortable):
    """Entry with pages and localizations."""

    category: Category

    def __post_init__(self):
        # load raw entry and ensure the category matches
        self.raw: RawEntry = RawEntry.load(self.path)
        if self.raw.category != self.category.id:
            raise ValueError(
                f"Entry {self.raw.name} has category {self.raw.category} but was initialized by {self.category.id}"
            )

        # localized strings
        self.name: LocalizedStr = self.i18n.localize(self.raw.name)

        # entries
        # TODO: make badn't
        self.pages: list[Page | dict[str, Any]] = []
        for page in self.raw.pages:
            if isinstance(page, str):
                page = Page_patchouli_text(
                    type="patchouli:text", text=self.book.format(page)
                )
            else:
                do_format(self.book, page, "text")
            do_localize(self.book, page, "title", "header")
            if page_transformer := page_transformers.get(page["type"]):
                page_transformer(self.book, page)
            self.pages.append(page)

    @property
    def book(self) -> Book:
        # implement WithBook
        return self.category.book

    @property
    def base_dir(self) -> Path:
        # implement WithPathId
        return self.book.entries_dir

    @property
    def cmp_key(self) -> tuple[bool, int, LocalizedStr]:
        # implement Sortable
        # note: python sorts false before true, so we invert priority
        return (not self.raw.priority, self.raw.sortnum, self.name)
