import re
from pathlib import Path
from typing import Annotated, Any, Self

from pydantic import (
    AfterValidator,
    Field,
    FieldValidationInfo,
    HttpUrl,
    field_validator,
)

from common.model import HexDocModel
from common.toml_placeholders import load_toml
from hexcasting.pattern import PatternStubFile
from minecraft.resource import ResourceLocation

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class PlatformProps(HexDocModel[Any]):
    resources: Path
    generated: Path
    src: Path
    package: Path
    pattern_stubs: list[PatternStubFile] | None = None


class I18nProps(HexDocModel[Any]):
    lang: str
    filename: str
    extra: dict[str, str] | None = None


class Properties(HexDocModel[Any]):
    modid: str
    book_name: str
    is_0_black: bool
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    recipe_dirs: list[Path]
    default_recipe_dir_index_: int = Field(alias="default_recipe_dir")

    pattern_regex: re.Pattern[str]

    template: str
    spoilers: set[ResourceLocation]
    blacklist: set[ResourceLocation]

    template_args: dict[str, Any]

    base_asset_urls: dict[str, NoTrailingSlashHttpUrl]
    """Mapping from modid to the url of that mod's `resources` directory on GitHub."""

    i18n: I18nProps

    common: PlatformProps
    fabric: PlatformProps  # TODO: some way to make these optional for addons
    forge: PlatformProps

    @classmethod
    def load(cls, path: Path) -> Self:
        return cls.model_validate(load_toml(path))

    @property
    def resources_dir(self):
        return self.common.resources

    @property
    def lang(self):
        return self.i18n.lang

    @property
    def book_dir(self) -> Path:
        """eg. `resources/data/hexcasting/patchouli_books/thehexbook`"""
        return (
            self.resources_dir
            / "data"
            / self.modid
            / "patchouli_books"
            / self.book_name
        )

    @property
    def categories_dir(self) -> Path:
        return self.book_dir / self.lang / "categories"

    @property
    def entries_dir(self) -> Path:
        return self.book_dir / self.lang / "entries"

    @property
    def templates_dir(self) -> Path:
        return self.book_dir / self.lang / "templates"

    @property
    def default_recipe_dir(self) -> Path:
        return self.recipe_dirs[self.default_recipe_dir_index_]

    @property
    def platforms(self) -> list[PlatformProps]:
        platforms = [self.common]
        if self.fabric:
            platforms.append(self.fabric)
        if self.forge:
            platforms.append(self.forge)
        return platforms

    @property
    def pattern_stubs(self) -> list[PatternStubFile]:
        return [
            stub
            for platform in self.platforms
            if platform.pattern_stubs
            for stub in platform.pattern_stubs
        ]

    @field_validator("default_recipe_dir_index_")
    def _check_default_recipe_dir(cls, value: int, info: FieldValidationInfo) -> int:
        num_dirs = len(info.data["recipe_dirs"])
        if value >= num_dirs:
            raise ValueError(
                f"default_recipe_dir must be a valid index of recipe_dirs (expected <={num_dirs - 1}, got {value})"
            )
        return value
