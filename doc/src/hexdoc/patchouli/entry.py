from typing import Iterable

from pydantic import Field, ValidationInfo, model_validator

from hexdoc.minecraft import LocalizedStr
from hexdoc.utils import Color, ItemStack, ResourceLocation
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.resource import HexDocIDModel
from hexdoc.utils.types import Sortable

from .book_context import BookContext
from .page.pages import Page


class Entry(HexDocIDModel, Sortable):
    """Entry json file, with pages and localizations.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/entry-json
    """

    is_spoiler: bool = False

    # required (entry.json)
    name: LocalizedStr
    category_id: ResourceLocation = Field(alias="category")
    icon: ItemStack
    pages: list[Page]

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
    def _cmp_key(self) -> tuple[bool, int, LocalizedStr]:
        # implement Sortable
        # note: python sorts false before true, so we invert priority
        return (not self.priority, self.sortnum, self.name)

    @property
    def anchors(self) -> Iterable[str]:
        for page in self.pages:
            if page.anchor is not None:
                yield page.anchor

    @model_validator(mode="after")
    def _check_is_spoiler(self, info: ValidationInfo):
        if not info.context or self.advancement is None:
            return self
        context = cast_or_raise(info.context, BookContext)

        self.is_spoiler = any(
            self.advancement.match(spoiler)
            for spoiler in context.spoilered_advancements
        )
        return self
