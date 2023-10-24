from typing import Iterable, Iterator

from pydantic import Field, ValidationInfo, model_validator

from hexdoc.core.resource import ItemStack, ResourceLocation
from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.recipe.abstract_recipes import CraftingRecipe
from hexdoc.model.inline import IDModel
from hexdoc.patchouli.page.abstract_pages import PageWithTitle
from hexdoc.patchouli.text.formatting import FormatTree
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.types import Color, Sortable

from .book_context import BookContext
from .page.pages import CraftingPage, Page


class Entry(IDModel, Sortable):
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

    def preprocess_pages(self) -> Iterator[Page]:
        """Combines adjacent CraftingPage recipes as much as possible."""
        accumulator = CraftingAccumulator.empty()

        for page in self.pages:
            match page:
                case CraftingPage(
                    recipes=list(recipes),
                    text=None,
                    title=None,
                    anchor=None,
                ):
                    accumulator.recipes += recipes
                case CraftingPage(
                    recipes=list(recipes),
                    title=LocalizedStr() as title,
                    text=None,
                    anchor=None,
                ):
                    if accumulator.recipes:
                        yield accumulator
                    accumulator = CraftingAccumulator.empty()
                    accumulator.recipes += recipes
                    accumulator.title = title
                case CraftingPage(
                    recipes=list(recipes),
                    title=None,
                    text=FormatTree() as text,
                    anchor=None,
                ):
                    accumulator.title = None
                    accumulator.text = text
                    accumulator.recipes += recipes
                    yield accumulator
                    accumulator = CraftingAccumulator.empty()
                case _:
                    if accumulator.recipes:
                        yield accumulator
                        accumulator = CraftingAccumulator.empty()
                    yield page

        if accumulator.recipes:
            yield accumulator

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


class CraftingAccumulator(PageWithTitle, template_type="patchouli:crafting"):
    recipes: list[CraftingRecipe] = Field(default_factory=list)

    @classmethod
    def empty(cls):
        return cls.model_construct()
