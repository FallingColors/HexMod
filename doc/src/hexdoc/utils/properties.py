from __future__ import annotations

import re
from pathlib import Path
from typing import Annotated, Any, Self

from pydantic import AfterValidator, Field, HttpUrl, field_validator

from .model import StripHiddenModel
from .resource import ResourceDir, ResourceLocation
from .toml_placeholders import load_toml_with_placeholders

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class PatternStubProps(StripHiddenModel):
    path: Path
    regex: re.Pattern[str]


class TemplateProps(StripHiddenModel):
    main: str
    dirs: list[Path] = Field(default_factory=list)
    packages: list[tuple[str, Path]]
    args: dict[str, Any]

    @field_validator("packages", mode="before")
    def _check_packages(cls, values: Any | list[Any]):
        if not isinstance(values, list):
            return values

        for i, value in enumerate(values):
            if isinstance(value, str):
                values[i] = (value, Path("_templates"))
        return values


class Properties(StripHiddenModel):
    modid: str
    book: ResourceLocation
    url: NoTrailingSlashHttpUrl
    default_lang: str
    is_0_black: bool = Field(default=False)
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    resource_dirs: list[ResourceDir]
    export_dir: Path

    entry_id_blacklist: set[ResourceLocation] = Field(default_factory=set)

    pattern_stubs: list[PatternStubProps]

    base_asset_urls: dict[str, NoTrailingSlashHttpUrl]
    """Mapping from modid to the url of that mod's `resources` directory on GitHub."""

    template: TemplateProps

    @classmethod
    def load(cls, path: Path) -> Self:
        return cls.model_validate(load_toml_with_placeholders(path))

    def mod_loc(self, path: str) -> ResourceLocation:
        """Returns a ResourceLocation with self.modid as the namespace."""
        return ResourceLocation(self.modid, path)

    def get_asset_url(self, id: ResourceLocation) -> str:
        base_url = self.base_asset_urls[id.namespace]
        return f"{base_url}/{id.file_path_stub('assets').as_posix()}"
