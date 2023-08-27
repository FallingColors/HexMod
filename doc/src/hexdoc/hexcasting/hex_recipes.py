from typing import Any, Literal

from hexdoc.minecraft import LocalizedItem, Recipe
from hexdoc.minecraft.recipe import (
    ItemIngredient,
    MinecraftItemIdIngredient,
    MinecraftItemTagIngredient,
)
from hexdoc.utils import HexdocModel, ResourceLocation, TypeTaggedUnion

# ingredients


class BrainsweepeeIngredient(
    TypeTaggedUnion,
    group="hexdoc.BrainsweepeeIngredient",
    type=None,
):
    pass


# lol, lmao
class VillagerIngredient(BrainsweepeeIngredient, type="villager"):
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


class EntityTypeIngredient(BrainsweepeeIngredient, type="entity_type"):
    entityType: ResourceLocation


class EntityTagIngredient(BrainsweepeeIngredient, type="entity_tag"):
    tag: ResourceLocation


class BlockStateIngredient(HexdocModel):
    # TODO: tagged union
    type: Literal["block"]
    block: ResourceLocation


_MinecraftItemIngredient = MinecraftItemIdIngredient | MinecraftItemTagIngredient
_MinecraftItemIngredientOrList = (
    _MinecraftItemIngredient | list[_MinecraftItemIngredient]
)


class ModConditionalIngredient(
    ItemIngredient,
    type="hexcasting:mod_conditional",
):
    default: _MinecraftItemIngredientOrList
    if_loaded: _MinecraftItemIngredientOrList
    modid: str


# results


class BlockState(HexdocModel):
    name: LocalizedItem
    properties: dict[str, Any] | None = None


# recipes


class BrainsweepRecipe(Recipe, type="hexcasting:brainsweep"):
    blockIn: BlockStateIngredient
    cost: int
    entityIn: BrainsweepeeIngredient
    result: BlockState
