from typing import Literal

from hexdoc.utils import HexdocModel, ResourceLocation

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


class ItemModel(HexdocModel, extra="ignore"):
    parent: ResourceLocation
    textures: dict[str, ResourceLocation] | None = None
