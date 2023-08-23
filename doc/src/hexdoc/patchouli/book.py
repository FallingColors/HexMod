from typing import Any, Literal, Self

from pydantic import Field, ValidationInfo, model_validator

from hexdoc.minecraft import I18n, LocalizedStr
from hexdoc.minecraft.i18n import I18nContext
from hexdoc.utils import (
    Color,
    HexDocModel,
    ItemStack,
    ModResourceLoader,
    ResLoc,
    ResourceLocation,
)
from hexdoc.utils.deserialize import cast_or_raise

from .book_context import BookContext
from .category import Category
from .entry import Entry
from .text import FormatTree


class Book(HexDocModel):
    """Main Patchouli book class.

    Includes all data from book.json, categories/entries/pages, and i18n.

    You should probably not use this (or any other Patchouli types, eg. Category, Entry)
    to edit and re-serialize book.json, because this class sets all the default values
    as defined by the docs. (TODO: superclass which doesn't do that)

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    # not in book.json
    i18n_data: I18n

    # required
    name: LocalizedStr
    landing_text: FormatTree
    use_resource_pack: Literal[True]

    # optional
    book_texture: ResourceLocation = ResLoc("patchouli", "textures/gui/book_brown.png")
    filler_texture: ResourceLocation | None = None
    crafting_texture: ResourceLocation | None = None
    model: ResourceLocation = ResLoc("patchouli", "book_brown")
    text_color: Color = Color("000000")
    header_color: Color = Color("333333")
    nameplate_color: Color = Color("FFDD00")
    link_color: Color = Color("0000EE")
    link_hover_color: Color = Color("8800EE")
    progress_bar_color: Color = Color("FFFF55")
    progress_bar_background: Color = Color("DDDDDD")
    open_sound: ResourceLocation | None = None
    flip_sound: ResourceLocation | None = None
    index_icon_: ResourceLocation | None = Field(default=None, alias="index_icon")
    pamphlet: bool = False
    show_progress: bool = True
    version: str | int = 0
    subtitle: LocalizedStr | None = None
    creative_tab: str | None = None
    advancements_tab: str | None = None
    dont_generate_book: bool = False
    custom_book_item: ItemStack | None = None
    show_toasts: bool = True
    use_blocky_font: bool = False
    i18n: bool = False
    macros: dict[str, str] = Field(default_factory=dict)
    pause_game: bool = False
    text_overflow_mode: Literal["overflow", "resize", "truncate"] | None = None

    @classmethod
    def load_all(cls, data: dict[str, Any], context: BookContext) -> Self:
        return cls.model_validate(data, context=context)

    @classmethod
    def load_book_json(cls, loader: ModResourceLoader, id: ResourceLocation):
        return loader.load_resource(
            type="data",
            folder="patchouli_books",
            id=id / "book",
        )

    @model_validator(mode="before")
    def _pre_root(cls, data: dict[str, Any], info: ValidationInfo) -> dict[str, Any]:
        if not info.context:
            return data
        context = cast_or_raise(info.context, I18nContext)

        return data | {
            "i18n_data": context.i18n,
        }

    @model_validator(mode="after")
    def _post_root(self, info: ValidationInfo) -> Self:
        """Loads categories and entries."""
        if not info.context:
            return self
        context = cast_or_raise(info.context, BookContext)

        # load categories
        self._categories: dict[ResourceLocation, Category] = Category.load_all(context)

        # load entries
        for resource_dir, id, data in context.loader.load_book_assets("entries"):
            entry = Entry.load(id, data, context)

            # i used the entry to insert the entry (pretty sure thanos said that)
            if not resource_dir.external:
                self._categories[entry.category_id].entries.append(entry)

        # we inserted a bunch of entries in no particular order, so sort each category
        for category in self._categories.values():
            category.entries.sort()

        return self

    @property
    def index_icon(self) -> ResourceLocation:
        # default value as defined by patchouli, apparently
        return self.model if self.index_icon_ is None else self.index_icon_

    @property
    def categories(self):
        return self._categories
