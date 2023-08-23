from __future__ import annotations

import re
from pathlib import Path
from typing import Annotated, Any, Self

from pydantic import AfterValidator, HttpUrl

from .model import HexDocModel, HexDocStripHiddenModel, HexDocValidationContext
from .resource import ResourceDir, ResourceLocation
from .toml_placeholders import load_toml_with_placeholders

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class HexDocMeta(HexDocModel):
    book_url: NoTrailingSlashHttpUrl


class PatternStubProps(HexDocStripHiddenModel):
    path: Path
    regex: re.Pattern[str]


class XplatProps(HexDocStripHiddenModel):
    src: Path
    pattern_stubs: list[PatternStubProps] | None = None
    resources: Path


class PlatformProps(XplatProps):
    recipes: Path
    tags: Path


class I18nProps(HexDocStripHiddenModel):
    default_lang: str


class Properties(HexDocStripHiddenModel):
    modid: str
    book: ResourceLocation
    url: NoTrailingSlashHttpUrl

    resource_dirs: list[ResourceDir]
    export_dir: Path

    entry_id_blacklist: set[ResourceLocation]

    template: str
    template_dirs: list[Path]
    template_packages: list[tuple[str, Path]]

    is_0_black: bool
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    template_args: dict[str, Any]

    base_asset_urls: dict[str, NoTrailingSlashHttpUrl]
    """Mapping from modid to the url of that mod's `resources` directory on GitHub."""

    i18n: I18nProps

    pattern_stubs: list[PatternStubProps]

    @classmethod
    def load(cls, path: Path) -> Self:
        return cls.model_validate(load_toml_with_placeholders(path))

    def mod_loc(self, path: str) -> ResourceLocation:
        """Returns a ResourceLocation with self.modid as the namespace."""
        return ResourceLocation(self.modid, path)

    def get_asset_url(self, id: ResourceLocation) -> str:
        base_url = self.base_asset_urls[id.namespace]
        return f"{base_url}/{id.file_path_stub('assets').as_posix()}"


class PropsContext(HexDocValidationContext):
    props: Properties
