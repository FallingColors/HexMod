from __future__ import annotations

from dataclasses import dataclass, field
from typing import Self

from common.deserialize import rename
from common.types import LocalizedStr, Sortable, sorted_dict
from minecraft.resource import ItemStack, ResourceLocation

from .entry import Entry
from .formatting import FormatTree
from .state import BookState, StatefulFile


@dataclass
class Category(StatefulFile[BookState], Sortable):
    """Category with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/category-json
    """

    # required
    name: LocalizedStr
    description: FormatTree
    icon: ItemStack

    # optional
    _parent_id: ResourceLocation | None = field(default=None, metadata=rename("parent"))
    parent: Category | None = field(default=None, init=False)
    flag: str | None = None
    sortnum: int = 0
    secret: bool = False

    def __post_init__(self):
        self.entries: list[Entry] = []

    @classmethod
    def load_all(cls, state: BookState):
        categories: dict[ResourceLocation, Self] = {}

        # load
        for path in state.props.categories_dir.rglob("*.json"):
            category = cls.load(path, state)
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
            self.props.categories_dir,
            self.path,
        )

    @property
    def _cmp_key(self) -> tuple[int, ...]:
        # implement Sortable
        if parent := self.parent:
            return parent._cmp_key + (self.sortnum,)
        return (self.sortnum,)
