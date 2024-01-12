from enum import Enum
from typing import Annotated, Any

from pydantic import BeforeValidator, PlainSerializer

from hexdoc.core import ResourceLocation
from hexdoc.model import HexdocModel


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

    def serialize(self):
        return self.name


DirectionField = Annotated[
    Direction,
    BeforeValidator(Direction.validate),
    PlainSerializer(Direction.serialize),
]


class BasePatternInfo(HexdocModel):
    startdir: DirectionField
    signature: str
    is_per_world: bool = False


class RawPatternInfo(BasePatternInfo):
    """Pattern info used in pattern pages."""

    q: int | None = None
    r: int | None = None


class PatternInfo(BasePatternInfo):
    """Pattern info used and exported by hexdoc for lookups."""

    id: ResourceLocation

    @property
    def name(self):
        return self.id.path
