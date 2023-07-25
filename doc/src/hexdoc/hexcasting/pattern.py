from enum import Enum
from typing import Annotated, Any

from pydantic import BeforeValidator

from hexdoc.utils import HexDocModel, ResourceLocation


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
