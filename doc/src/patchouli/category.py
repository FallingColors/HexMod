from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Self

import patchouli
from common.deserialize import from_dict_checked, load_json_data, rename
from common.formatting import FormatTree
from common.types import LocalizedStr, Sortable
from minecraft.resource import ItemStack, ResourceLocation


@dataclass
class Category(Sortable, patchouli.BookHelpers):
    """Category with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/category-json
    """

    # non-json fields
    path: Path
    book: patchouli.Book

    # required (category.json)
    name: LocalizedStr
    description: FormatTree
    icon: ItemStack

    # optional (category.json)
    parent_id: ResourceLocation | None = field(default=None, metadata=rename("parent"))
    flag: str | None = None
    sortnum: int = 0
    secret: bool = False

    @classmethod
    def load(cls, path: Path, book: patchouli.Book) -> Self:
        # load the raw data from json, and add our extra fields
        data = load_json_data(cls, path, {"path": path, "book": book})
        return from_dict_checked(cls, data, book.config(), path)

    def __post_init__(self):
        # load entries
        entry_dir = self.book.entries_dir / self.id.path
        self.entries: list[patchouli.Entry] = sorted(
            patchouli.Entry.load(path, self) for path in entry_dir.glob("*.json")
        )

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation.from_file(
            self.props.modid,
            self.book.categories_dir,
            self.path,
        )

    def parent(self) -> Category | None:
        """Get this category's parent from the book. Must not be called until the book
        is fully initialized."""
        if self.parent_id is None:
            return None
        return self.book.categories[self.parent_id]

    @property
    def _cmp_key(self) -> tuple[int, ...]:
        # implement Sortable
        if parent := self.parent():
            return parent._cmp_key + (self.sortnum,)
        return (self.sortnum,)
