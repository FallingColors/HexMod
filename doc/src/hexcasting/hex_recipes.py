from dataclasses import dataclass
from typing import Any, Literal

from common.types import LocalizedItem
from minecraft.recipe import Recipe
from minecraft.resource import ResourceLocation

from .hex_state import HexBookState


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


@dataclass(kw_only=True)
class BlockState:
    name: LocalizedItem
    properties: dict[str, Any] | None = None


@dataclass
class BrainsweepRecipe(Recipe[HexBookState], type="hexcasting:brainsweep"):
    blockIn: BlockStateIngredient
    villagerIn: VillagerIngredient
    result: BlockState
