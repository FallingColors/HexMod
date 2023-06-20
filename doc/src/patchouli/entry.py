from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Self

import patchouli
from common.deserialize import from_dict_checked, load_json_data, rename
from common.types import Color, LocalizedStr, Sortable
from minecraft.resource import ItemStack, ResourceLocation


@dataclass
class Entry(Sortable, patchouli.BookHelpers):
    """Entry json file, with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/entry-json
    """

    # non-json fields
    path: Path
    category: patchouli.Category

    # required (entry.json)
    name: LocalizedStr
    category_id: ResourceLocation = field(metadata=rename("category"))
    icon: ItemStack
    pages: list[patchouli.Page]

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
    def load(cls, path: Path, category: patchouli.Category) -> Self:
        # load the raw data from json, and add our extra fields
        data = load_json_data(cls, path, {"path": path, "category": category})
        config = category.book.config()
        return from_dict_checked(cls, data, config, path)

    def __post_init__(self):
        # check the category id, just for fun
        # note the _ and . on the left and right respectively
        if self.category_id != self.category.id:
            raise ValueError(
                f"Entry {self.name} has category {self.category_id} but was initialized by {self.category.id}"
            )

    @property
    def book(self) -> patchouli.Book:
        return self.category.book

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation.from_file(
            self.props.modid, self.book.entries_dir, self.path
        )

    @property
    def _cmp_key(self) -> tuple[bool, int, LocalizedStr]:
        # implement Sortable
        # note: python sorts false before true, so we invert priority
        return (not self.priority, self.sortnum, self.name)
