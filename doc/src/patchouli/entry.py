from __future__ import annotations

from pathlib import Path

from pydantic import Field

from common.properties import Properties
from common.types import Color, Sortable
from minecraft.i18n import LocalizedStr
from minecraft.resource import ItemStack, ResourceLocation

from .context import BookContext, BookModelFile
from .page import Page


class Entry(BookModelFile[BookContext, BookContext], Sortable):
    """Entry json file, with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/entry-json
    """

    # required (entry.json)
    name: LocalizedStr
    category_id: ResourceLocation = Field(alias="category")
    icon: ItemStack
    pages: list[Page[BookContext]]

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
    def _id_base_dir(cls, props: Properties) -> Path:
        return props.entries_dir

    @property
    def _cmp_key(self) -> tuple[bool, int, LocalizedStr]:
        # implement Sortable
        # note: python sorts false before true, so we invert priority
        return (not self.priority, self.sortnum, self.name)
