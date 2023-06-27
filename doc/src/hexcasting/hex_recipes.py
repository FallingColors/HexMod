from dataclasses import dataclass
from typing import Any, Literal

from common.types import LocalizedItem
from minecraft.recipe import Recipe
from minecraft.recipe.ingredients import (
    ItemIngredient,
    MinecraftItemIdIngredient,
    MinecraftItemTagIngredient,
)
from minecraft.resource import ResourceLocation

from .hex_state import HexBookState

# ingredients


@dataclass
class VillagerIngredient:  # lol, lmao
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


@dataclass
class BlockStateIngredient:
    # TODO: StateIngredient should also be a TypeTaggedUnion, probably
    type: Literal["block"]
    block: ResourceLocation


_MinecraftItemIngredient = MinecraftItemIdIngredient | MinecraftItemTagIngredient
_MinecraftItemIngredientOrList = (
    _MinecraftItemIngredient | list[_MinecraftItemIngredient]
)


@dataclass
class ModConditionalIngredient(
    ItemIngredient[HexBookState],
    type="hexcasting:mod_conditional",
):
    default: _MinecraftItemIngredientOrList
    if_loaded: _MinecraftItemIngredientOrList
    modid: str


# results


@dataclass(kw_only=True)
class BlockState:
    name: LocalizedItem
    properties: dict[str, Any] | None = None


# recipes


@dataclass
class BrainsweepRecipe(Recipe[HexBookState], type="hexcasting:brainsweep"):
    blockIn: BlockStateIngredient
    villagerIn: VillagerIngredient
    result: BlockState
