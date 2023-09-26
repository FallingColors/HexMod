from __future__ import annotations

import os
import re
from functools import cached_property
from pathlib import Path
from typing import Annotated, Any, Self

from pydantic import AfterValidator, Field, HttpUrl, TypeAdapter, field_validator

from .cd import RelativePath, RelativePathContext
from .model import DEFAULT_CONFIG, StripHiddenModel
from .resource import ResourceDir, ResourceLocation
from .toml_placeholders import load_toml_with_placeholders

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class PatternStubProps(StripHiddenModel):
    path: RelativePath
    regex: re.Pattern[str]
    per_world_value: str | None = "true"


class TemplateProps(StripHiddenModel):
    main: str
    static_dir: RelativePath | None = None
    dirs: list[RelativePath] = Field(default_factory=list)
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
    fallback_url: NoTrailingSlashHttpUrl
    default_lang: str
    is_0_black: bool = Field(default=False)
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    resource_dirs: list[ResourceDir]
    export_dir: RelativePath | None = None

    pattern_stubs: list[PatternStubProps]

    entry_id_blacklist: set[ResourceLocation] = Field(default_factory=set)

    base_asset_urls: dict[str, NoTrailingSlashHttpUrl]
    """Mapping from modid to the url of that mod's `resources` directory on GitHub."""

    template: TemplateProps

    @classmethod
    def load(cls, path: Path) -> Self:
        return cls.model_validate(
            load_toml_with_placeholders(path),
            context=RelativePathContext(root=path.parent),
        )

    def mod_loc(self, path: str) -> ResourceLocation:
        """Returns a ResourceLocation with self.modid as the namespace."""
        return ResourceLocation(self.modid, path)

    def get_asset_url(self, id: ResourceLocation) -> str:
        base_url = self.base_asset_urls[id.namespace]
        return f"{base_url}/{id.file_path_stub('assets').as_posix()}"

    @cached_property
    def url(self):
        github_pages_url = os.getenv("GITHUB_PAGES_URL", self.fallback_url)

        ta = TypeAdapter(NoTrailingSlashHttpUrl, config=DEFAULT_CONFIG)
        return ta.validate_python(github_pages_url)
