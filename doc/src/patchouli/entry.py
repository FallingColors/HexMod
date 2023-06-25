from __future__ import annotations

from dataclasses import dataclass, field

from common.deserialize import rename
from common.state import BookState, StatefulFile
from common.types import Color, LocalizedStr, Sortable
from minecraft.resource import ItemStack, ResourceLocation

from .page import Page


@dataclass
class Entry(StatefulFile[BookState], Sortable):
    """Entry json file, with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/entry-json
    """

    # required (entry.json)
    name: LocalizedStr
    category_id: ResourceLocation = field(metadata=rename("category"))
    icon: ItemStack
    pages: list[Page[BookState]]

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

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation.from_file(
            self.props.modid, self.props.entries_dir, self.path
        )

    @property
    def _cmp_key(self) -> tuple[bool, int, LocalizedStr]:
        # implement Sortable
        # note: python sorts false before true, so we invert priority
        return (not self.priority, self.sortnum, self.name)
