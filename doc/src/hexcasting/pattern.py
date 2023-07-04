import re
from enum import Enum
from pathlib import Path
from typing import Annotated, Any, Generator

from pydantic import BeforeValidator

from common.model import HexDocModel
from minecraft.resource import ResourceLocation


class Direction(Enum):
    NORTH_EAST = 0
    EAST = 1
    SOUTH_EAST = 2
    SOUTH_WEST = 3
    WEST = 4
    NORTH_WEST = 5

    @classmethod
    def validate(cls, value: str | int | Any):
        match value:
            case str():
                return cls[value]
            case int():
                return cls(value)
            case _:
                return value


DirectionField = Annotated[Direction, BeforeValidator(Direction.validate)]


class RawPatternInfo(HexDocModel[Any]):
    startdir: DirectionField
    signature: str
    is_per_world: bool = False
    q: int | None = None
    r: int | None = None


class PatternInfo(RawPatternInfo):
    id: ResourceLocation

    @property
    def name(self):
        return self.id.path


class PatternStubFile(HexDocModel[Any]):
    file: Path

    def load_patterns(
        self,
        modid: str,
        pattern_re: re.Pattern[str],
    ) -> Generator[PatternInfo, None, None]:
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.

        pattern_data = self.file.read_text("utf-8")
        for match in pattern_re.finditer(pattern_data):
            signature, startdir, name, is_per_world = match.groups()
            yield PatternInfo(
                startdir=Direction[startdir],
                signature=signature,
                is_per_world=bool(is_per_world),
                id=ResourceLocation(modid, name),
            )
