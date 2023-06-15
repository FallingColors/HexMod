from __future__ import annotations

from dataclasses import InitVar, dataclass, field
from pathlib import Path
from typing import Any, Self

from common.deserialize import from_dict_checked, load_json_data, rename
from common.types import Book, Category, Color, Sortable
from dacite import DaciteError, from_dict
from minecraft.i18n import LocalizedStr
from minecraft.resource import ItemStack, ResourceLocation
from patchouli.page import Page, Page_patchouli_text, page_transformers


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


@dataclass
class Entry(Sortable):
    """Entry json file, with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/entry-json
    """

    # non-json fields
    path: Path
    category: Category

    # required (entry.json)
    name: LocalizedStr
    category_id: ResourceLocation = field(metadata=rename("category"))
    icon: ItemStack
    # TODO: type
    _pages: list[dict[str, Any] | str] = field(metadata=rename("pages"))

    # optional (entry.json)
    advancement: ResourceLocation | None = None
    flag: str | None = None
    priority: bool = False
    secret: bool = False
    read_by_default: bool = False
    sortnum: int = 0
    turnin: ResourceLocation | None = None
    extra_recipe_mappings: dict[ItemStack, int] | None = None
    entry_color: Color | None = None  # this is undocumented lmao

    @classmethod
    def load(cls, path: Path, category: Category) -> Self:
        # load the raw data from json, and add our extra fields
        data = load_json_data(cls, path, {"path": path, "category": category})
        return from_dict_checked(cls, data, category.book.config(), path)

    def __post_init__(self):
        # check the category id, just for fun
        if self.category_id != self.category.id:
            raise ValueError(
                f"Entry {self.name} has category {self.category_id} but was initialized by {self.category.id}"
            )

        # entries
        # TODO: make badn't
        self.pages: list[Page | dict[str, Any]] = []
        for page in self._pages:
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
        return self.category.book

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation.file_id(
            self.book.modid, self.book.entries_dir, self.path
        )

    @property
    def _cmp_key(self) -> tuple[bool, int, LocalizedStr]:
        # implement Sortable
        # note: python sorts false before true, so we invert priority
        return (not self.priority, self.sortnum, self.name)
