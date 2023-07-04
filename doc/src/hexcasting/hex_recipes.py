from typing import Any, Literal

from common.model import HexDocModel
from hexcasting.hex_book import HexContext
from minecraft.i18n import LocalizedItem
from minecraft.recipe import (
    ItemIngredient,
    MinecraftItemIdIngredient,
    MinecraftItemTagIngredient,
    Recipe,
)
from minecraft.resource import ResourceLocation

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
