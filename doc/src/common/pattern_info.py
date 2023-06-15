import re
from dataclasses import dataclass
from pathlib import Path
from typing import Generator

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
    loader: str | None
    raw_path: str

    def path(self, resource_dir: Path) -> Path:
        file = resource_dir.parent / "java" / self.raw_path
        if self.loader is not None:
            file = Path(str(file).replace("Common", self.loader))
        return file

    def load_patterns(
        self,
        book: Book,
        pattern_re: re.Pattern[str],
    ) -> Generator[PatternInfo, None, None]:
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.

        pattern_data = self.path(book.resource_dir).read_text("utf-8")
        for match in pattern_re.finditer(pattern_data):
            angle_sig, direction, name, is_per_world = match.groups()
            yield PatternInfo(
                direction,
                angle_sig,
                bool(is_per_world),
                ResourceLocation(book.modid, name),
            )
