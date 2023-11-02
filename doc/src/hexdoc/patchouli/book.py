from collections import defaultdict
from typing import Any, Literal, Mapping, Self

from pydantic import (
    Field,
    PrivateAttr,
    ValidationInfo,
    field_validator,
    model_validator,
)

from hexdoc.core.compat import HexVersion
from hexdoc.core.loader import ModResourceLoader
from hexdoc.core.resource import ItemStack, ResLoc, ResourceLocation
from hexdoc.minecraft import LocalizedStr
from hexdoc.model import HexdocModel
from hexdoc.patchouli.text import BookLinkBases
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.types import Color, sorted_dict

from .book_context import BookContext
from .category import Category
from .entry import Entry
from .text import FormatTree


class Book(HexdocModel):
    """Main Patchouli book class.

    Includes all data from book.json, categories/entries/pages, and i18n.

    You should probably not use this (or any other Patchouli types, eg. Category, Entry)
    to edit and re-serialize book.json, because this class sets all the default values
    as defined by the docs.

    See: https://vazkiimods.github.io/Patchouli/docs/reference/book-json
    """

    # not in book.json
    id: ResourceLocation

    _link_bases: BookLinkBases = PrivateAttr(default_factory=dict)
    _categories: dict[ResourceLocation, Category] = PrivateAttr(default_factory=dict)

    # required
    name: LocalizedStr
    landing_text: FormatTree

    # required in 1.20 but optional in 1.19
    # so we'll make it optional and validate later
    use_resource_pack: bool = False

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
    index_icon: ResourceLocation | None = None
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
    def load_book_json(cls, loader: ModResourceLoader, id: ResourceLocation):
        data = cls._load_book_resource(loader, id)

        if HexVersion.get() <= HexVersion.v0_10_x and "extend" in data:
            id = loader.book_id = ResourceLocation.model_validate(data["extend"])
            return cls._load_book_resource(loader, id)

        return data

    @classmethod
    def load_all_from_data(cls, data: Mapping[str, Any], context: BookContext) -> Self:
        book = cls.model_validate(data, context=context)
        book._load_categories(context)
        book._load_entries(context)
        return book

    @classmethod
    def _load_book_resource(cls, loader: ModResourceLoader, id: ResourceLocation):
        _, data = loader.load_resource("data", "patchouli_books", id / "book")
        return data | {"id": id}

    @property
    def categories(self):
        return self._categories

    @property
    def link_bases(self):
        return self._link_bases

    def _load_categories(self, context: BookContext):
        categories = Category.load_all(context, self.id, self.use_resource_pack)

        if not categories:
            raise ValueError(
                "No categories found, are the paths in your properties file correct?"
            )

        for id, category in categories.items():
            self._categories[id] = category
            self.link_bases[(id, None)] = context.get_link_base(category.resource_dir)

    def _load_entries(self, context: BookContext):
        internal_entries = defaultdict[ResLoc, dict[ResLoc, Entry]](dict)

        for resource_dir, id, data in context.loader.load_book_assets(
            book_id=self.id,
            folder="entries",
            use_resource_pack=self.use_resource_pack,
        ):
            entry = Entry.load(resource_dir, id, data, context)

            # i used the entry to insert the entry (pretty sure thanos said that)
            if resource_dir.internal:
                internal_entries[entry.category_id][entry.id] = entry

            link_base = context.get_link_base(resource_dir)
            self._link_bases[(id, None)] = link_base
            for page in entry.pages:
                if page.anchor is not None:
                    self._link_bases[(id, page.anchor)] = link_base

        if not internal_entries:
            raise ValueError(
                f"No internal entries found for book {self.id}, is this the correct id?"
            )

        for category_id, new_entries in internal_entries.items():
            category = self._categories[category_id]
            category.entries = sorted_dict(category.entries | new_entries)

    @model_validator(mode="before")
    @classmethod
    def _pre_root(cls, data: dict[Any, Any] | Any):
        if isinstance(data, dict) and "index_icon" not in data:
            data["index_icon"] = data.get("model")
        return data

    @field_validator("use_resource_pack", mode="after")
    def _check_use_resource_pack(cls, value: bool):
        if HexVersion.get() >= HexVersion.v0_11_x and not value:
            raise ValueError(f"use_resource_pack must be True on this version")
        return value

    @model_validator(mode="after")
    def _post_root(self, info: ValidationInfo):
        if not info.context:
            return self
        context = cast_or_raise(info.context, BookContext)

        # make the macros accessible when rendering the template
        self.macros |= context.macros

        return self
