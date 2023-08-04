from typing import Any, Literal

from hexdoc.minecraft import LocalizedItem, Recipe
from hexdoc.minecraft.recipe import (
    ItemIngredient,
    MinecraftItemIdIngredient,
    MinecraftItemTagIngredient,
)
from hexdoc.utils import HexDocModel, ResourceLocation

from .hex_book import HexContext

# ingredients


class VillagerIngredient(HexDocModel[HexContext]):  # lol, lmao
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


class BlockStateIngredient(HexDocModel[HexContext]):
    # TODO: StateIngredient should also be a TypeTaggedUnion, probably
    type: Literal["block"]
    block: ResourceLocation


_MinecraftItemIngredient = MinecraftItemIdIngredient | MinecraftItemTagIngredient
_MinecraftItemIngredientOrList = (
    _MinecraftItemIngredient | list[_MinecraftItemIngredient]
)


class ModConditionalIngredient(
    ItemIngredient[HexContext],
    type="hexcasting:mod_conditional",
):
    default: _MinecraftItemIngredientOrList
    if_loaded: _MinecraftItemIngredientOrList
    modid: str


# results


class BlockState(HexDocModel[HexContext]):
    name: LocalizedItem
    properties: dict[str, Any] | None = None


# recipes


class BrainsweepRecipe(Recipe[HexContext], type="hexcasting:brainsweep"):
    blockIn: BlockStateIngredient
    villagerIn: VillagerIngredient
    result: BlockState
