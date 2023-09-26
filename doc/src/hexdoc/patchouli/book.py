from typing import Any, Literal, Self

from pydantic import Field, ValidationInfo, field_validator, model_validator

from hexdoc.minecraft import I18n, LocalizedStr
from hexdoc.minecraft.i18n import I18nContext
from hexdoc.utils import (
    Color,
    HexdocModel,
    ItemStack,
    ModResourceLoader,
    ResLoc,
    ResourceLocation,
)
from hexdoc.utils.compat import HexVersion
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.types import sorted_dict

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
    i18n_data: I18n

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
    def load_all(cls, data: dict[str, Any], context: BookContext) -> Self:
        return cls.model_validate(data, context=context)

    @classmethod
    def load_book_json(cls, loader: ModResourceLoader, id: ResourceLocation):
        resource_dir, data = cls._load_book_json(loader, id)

        if HexVersion.get() <= HexVersion.v0_10_x and "extend" in data:
            id = ResourceLocation.from_str(cast_or_raise(data["extend"], str))
            resource_dir, data = cls._load_book_json(loader, id)

        return resource_dir, data | {"id": id}

    @classmethod
    def _load_book_json(cls, loader: ModResourceLoader, id: ResourceLocation):
        return loader.load_resource(
            type="data",
            folder="patchouli_books",
            id=id / "book",
        )

    @model_validator(mode="before")
    def _pre_root(cls, data: Any, info: ValidationInfo):
        if not info.context:
            return data
        context = cast_or_raise(info.context, I18nContext)

        match data:
            case {**values}:
                return values | {
                    "i18n_data": context.i18n,
                    "index_icon": values.get("index_icon") or values.get("model"),
                }
            case _:
                return data

    @field_validator("use_resource_pack", mode="after")
    def _check_use_resource_pack(cls, value: bool):
        if HexVersion.get() >= HexVersion.v0_11_x and not value:
            raise ValueError(f"use_resource_pack must be True on this version")
        return value

    @model_validator(mode="after")
    def _load_categories_and_entries(self, info: ValidationInfo) -> Self:
        if not info.context:
            return self
        context = cast_or_raise(info.context, BookContext)

        # make the macros accessible when rendering the template
        self.macros |= context.macros

        self._link_bases: dict[tuple[ResourceLocation, str | None], str] = {}

        # load categories
        self._categories = Category.load_all(context, self.id, self.use_resource_pack)
        for id, category in self._categories.items():
            self._link_bases[(id, None)] = context.loader.get_link_base(
                category.resource_dir
            )

        if not self._categories:
            raise ValueError(
                "No categories found. "
                "Ensure the paths in your properties file are correct."
            )

        # load entries
        found_internal_entries = self._load_all_entries(context)
        if not found_internal_entries:
            raise ValueError(
                "No internal entries found. "
                "Ensure the paths in your properties file are correct."
            )

        # we inserted a bunch of entries in no particular order, so sort each category
        for category in self._categories.values():
            category.entries = sorted_dict(category.entries)

        return self

    def _load_all_entries(self, context: BookContext):
        found_internal_entries = False

        for resource_dir, id, data in context.loader.load_book_assets(
            self.id,
            "entries",
            self.use_resource_pack,
        ):
            entry = Entry.load(resource_dir, id, data, context)

            link_base = context.loader.get_link_base(resource_dir)
            self._link_bases[(id, None)] = link_base
            for page in entry.pages:
                if page.anchor is not None:
                    self._link_bases[(id, page.anchor)] = link_base

            # i used the entry to insert the entry (pretty sure thanos said that)
            if not resource_dir.external:
                found_internal_entries = True
                self._categories[entry.category_id].entries[entry.id] = entry

        return found_internal_entries

    @property
    def categories(self):
        # this exists because otherwise Pydantic complains that we're assigning to a
        # nonexistent field; it ignores underscore-prefixed fields
        return self._categories

    @property
    def link_bases(self):
        return self._link_bases
