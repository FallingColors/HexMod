from __future__ import annotations

import logging
import re
from pathlib import Path
from typing import Annotated, Any, Self

from pydantic import AfterValidator, Field, HttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

from .cd import RelativePath, relative_path_root
from .model import StripHiddenModel
from .resource import ResourceDir, ResourceLocation
from .toml_placeholders import load_toml_with_placeholders

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class EnvironmentVariableProps(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env")

    # default Actions environment variables
    github_repository: str
    github_sha: str

    # set by CI
    github_pages_url: NoTrailingSlashHttpUrl

    # optional for debugging
    debug_githubusercontent: str | None = None

    @classmethod
    def model_validate_env(cls):
        return cls.model_validate({})

    @property
    def asset_url(self):
        if self.debug_githubusercontent is not None:
            return self.debug_githubusercontent

        return (
            f"https://raw.githubusercontent.com"
            f"/{self.repo_owner}/{self.repo_name}/{self.github_sha}"
        )

    @property
    def repo_owner(self):
        return self._github_repository_parts[0]

    @property
    def repo_name(self):
        return self._github_repository_parts[1]

    @property
    def _github_repository_parts(self):
        owner, repo_name = self.github_repository.split("/", maxsplit=1)
        return owner, repo_name


class PatternStubProps(StripHiddenModel):
    path: RelativePath
    regex: re.Pattern[str]
    per_world_value: str | None = "true"


class TemplateProps(StripHiddenModel):
    main: str = "main.html.jinja"
    style: str = "main.css.jinja"

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


class MinecraftAssetsProps(StripHiddenModel):
    ref: str
    version: str


class GaslightingProps(StripHiddenModel):
    id: str
    variants: int


class TexturesProps(StripHiddenModel):
    missing: list[ResourceLocation]
    override: dict[ResourceLocation, ResourceLocation]
    gaslighting: dict[ResourceLocation, GaslightingProps]


class Properties(StripHiddenModel):
    env: EnvironmentVariableProps

    modid: str
    book: ResourceLocation
    default_lang: str
    is_0_black: bool = Field(default=False)
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    resource_dirs: list[ResourceDir]
    export_dir: RelativePath | None = None

    pattern_stubs: list[PatternStubProps]

    entry_id_blacklist: set[ResourceLocation] = Field(default_factory=set)

    minecraft_assets: MinecraftAssetsProps

    # FIXME: remove this and get the data from the actual model files
    textures: TexturesProps

    template: TemplateProps

    @classmethod
    def load(cls, path: Path) -> Self:
        with relative_path_root(path.parent):
            env = EnvironmentVariableProps.model_validate_env()
            props = cls.model_validate(
                load_toml_with_placeholders(path) | {"env": env},
            )

        logging.getLogger(__name__).debug(props)
        return props

    def mod_loc(self, path: str) -> ResourceLocation:
        """Returns a ResourceLocation with self.modid as the namespace."""
        return ResourceLocation(self.modid, path)

    @property
    def url(self):
        return self.env.github_pages_url
