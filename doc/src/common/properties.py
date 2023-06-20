from __future__ import annotations

import re
from dataclasses import InitVar, dataclass, field
from pathlib import Path
from typing import Self

from common.deserialize import TypedConfig, from_dict_checked, load_toml_data, rename
from common.pattern import PatternStubFile
from common.types import LocalizedStr


@dataclass
class PlatformProps:
    resources: Path
    generated: Path
    src: Path
    package: Path
    pattern_stubs: list[PatternStubFile] | None = None


@dataclass
class I18nProps:
    lang: str
    filename: str
    extra: dict[str, LocalizedStr] | None = None


@dataclass(kw_only=True)
class Properties:
    modid: str
    book_name: str
    template: Path
    recipe_dirs: list[Path]

    _pattern_regex: InitVar[str] = field(metadata=rename("pattern_regex"))
    pattern_re: re.Pattern[str] = field(init=False)

    i18n: I18nProps

    common: PlatformProps
    fabric: PlatformProps  # TODO: non-shitty way to make these optional for addons
    forge: PlatformProps

    def __post_init__(self, _pattern_regex: str):
        object.__setattr__(self, "pattern_re", re.compile(_pattern_regex))

    @classmethod
    def load(cls, path: Path) -> Self:
        data = load_toml_data(cls, path)
        config = TypedConfig(cast=[LocalizedStr, Path])
        return from_dict_checked(cls, data, config)

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
