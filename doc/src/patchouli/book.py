from typing import Any, Generic, Literal, Self, cast

from pydantic import Field, ValidationInfo, model_validator

from common.deserialize import isinstance_or_raise, load_json
from common.model import AnyContext, HexDocModel
from common.properties import Properties
from common.types import Color
from minecraft.i18n import I18n, LocalizedStr
from minecraft.resource import ItemStack, ResLoc, ResourceLocation

from .category import Category
from .context import AnyBookContext, BookContext
from .entry import Entry
from .text import DEFAULT_MACROS, FormatTree


class Book(Generic[AnyContext, AnyBookContext], HexDocModel[AnyBookContext]):
    """Main Patchouli book class.

    Includes all data from book.json, categories/entries/pages, and i18n.

    You should probably not use this (or any other Patchouli types, eg. Category, Entry)
    to edit and re-serialize book.json, because this class sets all the default values
    as defined by the docs. (TODO: superclass which doesn't do that)

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    # not in book.json
    context: AnyBookContext = Field(default_factory=dict)
    categories: dict[ResourceLocation, Category] = Field(default_factory=dict)

    # required
    name: LocalizedStr
    landing_text: FormatTree

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
    creative_tab: str = "misc"  # TODO: this was changed in 1.19.3+, and again in 1.20
    advancements_tab: str | None = None
    dont_generate_book: bool = False
    custom_book_item: ItemStack | None = None
    show_toasts: bool = True
    use_blocky_font: bool = False
    do_i18n: bool = Field(default=False, alias="i18n")
    macros: dict[str, str] = Field(default_factory=dict)
    pause_game: bool = False
    text_overflow_mode: Literal["overflow", "resize", "truncate"] | None = None
    extend: str | None = None
    """NOTE: currently this WILL NOT load values from the target book!"""
    allow_extensions: bool = True

    @classmethod
    def load(cls, data: dict[str, Any], context: AnyBookContext):
        return cls.model_validate(data, context=context)

    @classmethod
    def prepare(cls, props: Properties) -> tuple[dict[str, Any], BookContext]:
        # read the raw dict from the json file
        path = props.book_dir / "book.json"
        data = load_json(path)
        assert isinstance_or_raise(data, dict[str, Any])

        # NOW we can convert the actual book data
        return data, {
            "i18n": I18n(props, data["i18n"]),
            "props": props,
            "macros": data["macros"] | DEFAULT_MACROS,
        }

    @model_validator(mode="after")
    def _post_root(self, info: ValidationInfo) -> Self:
        """Loads categories and entries."""
        context = cast(AnyBookContext, info.context)
        if not context:
            return self
        self.context = context

        # categories
        self.categories = Category.load_all(context)

        # entries
        for path in context["props"].entries_dir.rglob("*.json"):
            # i used the entry to insert the entry (pretty sure thanos said that)
            entry = Entry.load(path, context)
            self.categories[entry.category_id].entries.append(entry)

        # we inserted a bunch of entries in no particular order, so sort each category
        for category in self.categories.values():
            category.entries.sort()

        return self

    @property
    def index_icon(self) -> ResourceLocation:
        # default value as defined by patchouli, apparently
        return self.model if self.index_icon_ is None else self.index_icon_
