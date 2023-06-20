from dataclasses import dataclass
from typing import Literal

from minecraft.resource import ResourceLocation


@dataclass
class ItemIngredientData:
    item: ResourceLocation | None = None
    tag: ResourceLocation | None = None


# TODO: tagged union~!
@dataclass
class ModConditionalIngredient:
    type: Literal["hexcasting:mod_conditional"]
    default: ItemIngredientData
    if_loaded: ItemIngredientData
    modid: str


ItemIngredient = (
    ItemIngredientData | ModConditionalIngredient | list[ItemIngredientData]
)


@dataclass
class VillagerIngredient:
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


@dataclass
class BlockStateIngredient:
    # TODO: StateIngredient should also be a TypeTaggedUnion, probably
    type: Literal["block"]
    block: ResourceLocation
