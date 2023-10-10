from typing import Literal

from hexdoc.core.resource import ResourceLocation
from hexdoc.model import HexdocModel

ItemDisplayPosition = Literal[
    "thirdperson_righthand",
    "thirdperson_lefthand",
    "firstperson_righthand",
    "firstperson_lefthand",
    "gui",
    "head",
    "ground",
    "fixed",
]

# TODO


class ItemModel(HexdocModel, extra="ignore"):
    parent: ResourceLocation
    textures: dict[str, ResourceLocation] | None = None
