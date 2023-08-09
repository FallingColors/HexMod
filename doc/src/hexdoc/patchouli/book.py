from importlib import resources
from importlib.metadata import entry_points
from typing import Any, Generic, Literal, Self, cast

from pydantic import Field, ValidationInfo, model_validator

from hexdoc.minecraft import I18n, LocalizedStr
from hexdoc.utils import (
    AnyContext,
    Color,
    HexDocModel,
    ItemStack,
    Properties,
    ResLoc,
    ResourceLocation,
)
from hexdoc.utils.deserialize import isinstance_or_raise, load_json_dict

from .book_models import AnyBookContext, BookContext
from .category import Category
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
    def prepare(cls, props: Properties) -> tuple[dict[str, Any], BookContext]:
        # read the raw dict from the json file
        path = props.find_resource("data", "patchouli_books", props.book / "book")
        data = load_json_dict(path)

        # set up the deserialization context object
        assert isinstance_or_raise(data["i18n"], bool)
        assert isinstance_or_raise(data["macros"], dict)
        context: BookContext = {
            "props": props,
            "i18n": I18n(props, data["i18n"]),
            "macros": DEFAULT_MACROS | data["macros"],
        }

        return data, context

    @classmethod
    def load(cls, data: dict[str, Any], context: AnyBookContext) -> Self:
        return cls.model_validate(data, context=context)

    @classmethod
    def from_id(cls, book_id: ResourceLocation) -> Self:
        # load the module for the given book id using the entry point
        # TODO: this is untested because it needs to change for 0.11 anyway :/
        books = entry_points(group="hexdoc.book_data")
        book_module = books[str(book_id)].load()

        # read and validate the actual data file
        book_path = resources.files(book_module) / book_module.BOOK_DATA_PATH
        return cls.model_validate_json(book_path.read_text("utf-8"))

    @model_validator(mode="before")
    def _pre_root(cls, data: dict[str, Any], info: ValidationInfo) -> dict[str, Any]:
        context = cast(AnyBookContext, info.context)
        if not context:
            return data

        return data | {
            "i18n_data": context["i18n"],
        }

    @model_validator(mode="after")
    def _post_root(self, info: ValidationInfo) -> Self:
        """Loads categories and entries."""
        context = cast(AnyBookContext, info.context)
        if not context:
            return self

        # load categories
        self._categories: dict[ResourceLocation, Category] = Category.load_all(context)

        # load entries
        for id, path in context["props"].find_book_assets("entries"):
            entry = Entry.load(id, path, context)
            # i used the entry to insert the entry (pretty sure thanos said that)
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
