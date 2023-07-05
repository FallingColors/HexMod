import re
from pathlib import Path
from typing import Any, Self

from pydantic import Field, model_validator

from common.model import HexDocModel
from common.toml_placeholders import load_toml
from hexcasting.pattern import PatternStubFile


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
    template: Path

    recipe_dirs: list[Path]
    default_recipe_dir_index_: int = Field(alias="default_recipe_dir")

    pattern_regex: re.Pattern[str]

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

    @model_validator(mode="after")
    def _check_default_recipe_dir(self):
        if self.default_recipe_dir_index_ >= len(self.recipe_dirs):
            raise ValueError(
                f"default_recipe_dir must be a valid index of recipe_dirs (expected <={len(self.recipe_dirs)}, got {self.default_recipe_dir_index_})"
            )
        return self
