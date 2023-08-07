from __future__ import annotations

import re
from pathlib import Path
from typing import Annotated, Any, Self, TypeVar

from pydantic import AfterValidator, Field, HttpUrl
from typing_extensions import TypedDict

from .model import HexDocModel
from .resource import ResourceLocation
from .toml_placeholders import load_toml_with_placeholders

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class PatternStubProps(HexDocModel[Any], extra="ignore"):
    path: Path
    regex: re.Pattern[str]


class XplatProps(HexDocModel[Any], extra="ignore"):
    src: Path
    package: Path
    pattern_stubs: list[PatternStubProps] | None = None
    resources: Path


class PlatformProps(XplatProps):
    recipes: Path
    tags: Path


class I18nProps(HexDocModel[Any], extra="ignore"):
    default_lang: str
    filename: str
    extra: dict[str, str] = Field(default_factory=dict)
    keys: dict[str, str] = Field(default_factory=dict)


class Properties(HexDocModel[Any], extra="ignore"):
    modid: str
    book_name: str
    url: NoTrailingSlashHttpUrl
    is_0_black: bool
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    template: str
    template_dirs: list[Path]
    template_packages: list[tuple[str, Path]]

    spoilered_advancements: set[ResourceLocation]
    entry_id_blacklist: set[ResourceLocation]

    template_args: dict[str, Any]

    base_asset_urls: dict[str, NoTrailingSlashHttpUrl]
    """Mapping from modid to the url of that mod's `resources` directory on GitHub."""

    i18n: I18nProps

    common: XplatProps
    fabric: PlatformProps  # TODO: some way to make these optional for addons
    forge: PlatformProps

    @classmethod
    def load(cls, path: Path) -> Self:
        return cls.model_validate(load_toml_with_placeholders(path))

    @property
    def resources_dir(self):
        return self.common.resources

    @property
    def lang(self):
        return self.i18n.default_lang

    @property
    def book_path(self) -> Path:
        """eg. `resources/data/hexcasting/patchouli_books/thehexbook/book.json`"""
        return (
            self.resources_dir
            / "data"
            / self.modid
            / "patchouli_books"
            / self.book_name
            / "book.json"
        )

    @property
    def assets_dir(self) -> Path:
        """eg. `resources/assets/hexcasting`"""
        return self.resources_dir / "assets" / self.modid

    @property
    def book_assets_dir(self) -> Path:
        """eg. `resources/assets/hexcasting/patchouli_books/thehexbook`"""
        return self.assets_dir / "patchouli_books" / self.book_name

    @property
    def categories_dir(self) -> Path:
        return self.book_assets_dir / self.lang / "categories"

    @property
    def entries_dir(self) -> Path:
        return self.book_assets_dir / self.lang / "entries"

    @property
    def platforms(self) -> list[XplatProps]:
        platforms = [self.common]
        if self.fabric:
            platforms.append(self.fabric)
        if self.forge:
            platforms.append(self.forge)
        return platforms

    @property
    def pattern_stubs(self):
        return [
            stub
            for platform in self.platforms
            if platform.pattern_stubs
            for stub in platform.pattern_stubs
        ]

    def asset_url(self, asset: ResourceLocation, path: str = "assets") -> str:
        base_url = self.base_asset_urls[asset.namespace]
        return f"{base_url}/{path}/{asset.full_path}"


class PropsContext(TypedDict):
    props: Properties


AnyPropsContext = TypeVar("AnyPropsContext", bound=PropsContext)
