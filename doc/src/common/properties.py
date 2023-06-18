import re
from dataclasses import InitVar, dataclass, field
from pathlib import Path
from typing import Self

import tomllib
from common.deserialize import (
    TypedConfig,
    from_dict_checked,
    handle_metadata_inplace,
    load_toml_data,
    rename,
)
from common.pattern import PatternStubFile
from common.toml_placeholders import TOMLTable, fill_placeholders
from common.types import LocalizedStr


@dataclass(frozen=True)
class PlatformProps:
    resources: Path
    generated: Path
    src: Path
    package: Path
    pattern_stubs: list[PatternStubFile] | None = None


@dataclass(frozen=True)
class I18nProps:
    lang: str
    dir: Path
    file: Path
    extra: dict[str, LocalizedStr] | None = None


@dataclass(kw_only=True, frozen=True)
class Properties:
    modid: str
    book_name: str
    template: Path

    _pattern_regex: InitVar[str] = field(metadata=rename("pattern_regex"))
    pattern_re: re.Pattern[str] = field(init=False)

    common: PlatformProps
    fabric: PlatformProps | None
    forge: PlatformProps | None

    i18n: I18nProps

    def __post_init__(self, _pattern_regex: str):
        object.__setattr__(self, "pattern_re", re.compile(_pattern_regex))

    @classmethod
    def load(cls, path: Path) -> Self:
        data = load_toml_data(cls, path)
        config = TypedConfig(cast=[LocalizedStr, Path])
        return from_dict_checked(cls, data, config)

    @property
    def resources(self):
        return self.common.resources

    @property
    def lang(self):
        return self.i18n.lang

    @property
    def platforms(self) -> list[PlatformProps]:
        platforms = [self.common]
        if self.fabric:
            platforms.append(self.fabric)
        if self.forge:
            platforms.append(self.forge)
        return platforms

    @property
    def pattern_stubs(self) -> dict[PlatformProps, PatternStubFile]:
        return {
            platform: stub
            for platform in self.platforms
            if platform.pattern_stubs
            for stub in platform.pattern_stubs
        }
