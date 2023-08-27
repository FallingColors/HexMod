from typing import Self

from pydantic import Field

from hexdoc.minecraft import LocalizedStr
from hexdoc.utils import ItemStack, LoaderContext, ResourceLocation
from hexdoc.utils.resource import HexdocIDModel
from hexdoc.utils.types import Sortable, sorted_dict

from .entry import Entry
from .text import FormatTree


class Category(HexdocIDModel, Sortable):
    """Category with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/category-json
    """

    entries: list[Entry] = Field(default_factory=list)

    # required
    name: LocalizedStr
    description: FormatTree
    icon: ItemStack

    # optional
    parent_id: ResourceLocation | None = Field(default=None, alias="parent")
    _parent_cmp_key: tuple[int, ...] | None = None
    flag: str | None = None
    sortnum: int = 0
    secret: bool = False

    @classmethod
    def load_all(cls, context: LoaderContext):
        categories: dict[ResourceLocation, Self] = {}

        # load
        for resource_dir, id, data in context.loader.load_book_assets("categories"):
            category = cls.load(resource_dir, id, data, context)
            categories[id] = category

        # late-init _parent_cmp_key
        # track iterations to avoid an infinite loop if for some reason there's a cycle
        # TODO: array of non-ready categories so we can give a better error message?
        done, iterations = False, 0
        while not done and (iterations := iterations + 1) < 1000:
            done = True
            for category in categories.values():
                # if we still need to init this category, get the parent
                if category._is_cmp_key_ready:
                    continue
                assert category.parent_id
                parent = categories[category.parent_id]

                # only set _parent_cmp_key if the parent has been initialized
                if parent._is_cmp_key_ready:
                    category._parent_cmp_key = parent._cmp_key
                else:
                    done = False

        if not done:
            raise RuntimeError(
                f"Possible circular dependency of category parents: {categories}"
            )

        # return sorted by sortnum, which requires parent to be initialized
        return sorted_dict(categories)

    @property
    def is_spoiler(self) -> bool:
        return all(entry.is_spoiler for entry in self.entries)

    @property
    def _is_cmp_key_ready(self) -> bool:
        return self.parent_id is None or self._parent_cmp_key is not None

    @property
    def _cmp_key(self) -> tuple[int, ...]:
        # implement Sortable
        if parent_cmp_key := self._parent_cmp_key:
            return parent_cmp_key + (self.sortnum,)
        return (self.sortnum,)
