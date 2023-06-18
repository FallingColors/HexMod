import re
from dataclasses import dataclass
from pathlib import Path
from typing import Generator

from common.properties import Properties
from common.types import Book
from minecraft.resource import ResourceLocation


@dataclass
class RawPatternInfo:
    direction: str
    angle_sig: str
    is_per_world: bool


@dataclass
class PatternInfo(RawPatternInfo):
    id: ResourceLocation


@dataclass
class PatternStubFile:
    file: Path

    def load_patterns(self, props: Properties) -> Generator[PatternInfo, None, None]:
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.

        pattern_data = self.file.read_text("utf-8")
        for match in props.pattern_re.finditer(pattern_data):
            angle_sig, direction, name, is_per_world = match.groups()
            yield PatternInfo(
                direction,
                angle_sig,
                bool(is_per_world),
                ResourceLocation(props.modid, name),
            )
