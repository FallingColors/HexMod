from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Self

import patchouli
from common.deserialize import from_dict_checked, load_json_data, rename
from common.formatting import FormatTree
from common.types import LocalizedStr, Sortable, sorted_dict
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
    _parent_id: ResourceLocation | None = field(default=None, metadata=rename("parent"))
    parent: Category | None = field(default=None, init=False)
    flag: str | None = None
    sortnum: int = 0
    secret: bool = False

    def __post_init__(self):
        self.entries: list[patchouli.Entry] = []

    @classmethod
    def _load(cls, path: Path, book: patchouli.Book) -> Self:
        # load the raw data from json, and add our extra fields
        data = load_json_data(cls, path, {"path": path, "book": book})
        return from_dict_checked(cls, data, book.config(), path)

    @classmethod
    def load_all(cls, book: patchouli.Book):
        categories: dict[ResourceLocation, Self] = {}

        # load
        for path in book.categories_dir.rglob("*.json"):
            category = cls._load(path, book)
            categories[category.id] = category

        # late-init parent
        for category in categories.values():
            if category._parent_id is not None:
                category.parent = categories[category._parent_id]

        # return sorted by sortnum, which requires parent to be initialized
        return sorted_dict(categories)

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation.from_file(
            self.props.modid,
            self.book.categories_dir,
            self.path,
        )

    @property
    def _cmp_key(self) -> tuple[int, ...]:
        # implement Sortable
        if parent := self.parent:
            return parent._cmp_key + (self.sortnum,)
        return (self.sortnum,)
