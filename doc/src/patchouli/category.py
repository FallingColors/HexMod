from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from common.composition import Book, WithBook
from common.deserialize import FromJson
from common.formatting import FormatTree
from common.utils import Sortable
from minecraft.i18n import LocalizedStr
from minecraft.resource import ItemStack, ResourceLocation, WithPathId
from patchouli.entry import Entry
from serde import deserialize


@deserialize
class RawCategory(FromJson):
    """Direct representation of a Category json file.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/category-json
    """

    # required
    name: str
    description: str
    icon: ItemStack = ItemStack.field()

    # optional
    parent: ResourceLocation | None = ResourceLocation.field(default=None)
    flag: str | None = None
    sortnum: int = 0
    secret: bool = False


@dataclass
class Category(WithBook, WithPathId, Sortable):
    """Category with pages and localizations."""

    _book: Book

    def __post_init__(self):
        self.raw: RawCategory = RawCategory.load(self.path)

        # localized strings
        self.name: LocalizedStr = self.i18n.localize(self.raw.name)
        self.description: FormatTree = self.book.format(self.raw.description)

        # entries
        self.entries: list[Entry] = self._load_entries()

    @property
    def parent(self) -> Category | None:
        if self.raw.parent is None:
            return None
        return self.book.categories[self.raw.parent]

    def _load_entries(self) -> list[Entry]:
        entry_dir = self.book.entries_dir / self.id.path
        return sorted(Entry(path, self) for path in entry_dir.glob("*.json"))

    @property
    def book(self) -> Book:
        # implement WithBook
        return self._book

    @property
    def base_dir(self) -> Path:
        # implement WithPathId
        return self.book.categories_dir

    @property
    def cmp_key(self) -> tuple[int, ...]:
        # implement Sortable
        if self.parent:
            return self.parent.cmp_key + (self.raw.sortnum,)
        return (self.raw.sortnum,)
