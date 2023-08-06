from typing import Any, Literal

from hexdoc.minecraft import LocalizedItem, Recipe
from hexdoc.minecraft.recipe import (
    ItemIngredient,
    MinecraftItemIdIngredient,
    MinecraftItemTagIngredient,
)
from hexdoc.utils import HexDocModel, ResourceLocation, TypeTaggedUnion
from hexdoc.utils.model import AnyContext

from .hex_book import HexContext

# ingredients


class BrainsweepeeIngredient(
    TypeTaggedUnion[AnyContext],
    group="hexdoc.BrainsweepeeIngredient",
    type=None,
):
    pass


# lol, lmao
class VillagerIngredient(BrainsweepeeIngredient[HexContext], type="villager"):
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


class EntityTypeIngredient(BrainsweepeeIngredient[HexContext], type="entity_type"):
    entityType: ResourceLocation


class EntityTagIngredient(BrainsweepeeIngredient[HexContext], type="entity_tag"):
    tag: ResourceLocation


class BlockStateIngredient(HexDocModel[HexContext]):
    # TODO: tagged union
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
    cost: int
    entityIn: BrainsweepeeIngredient[HexContext]
    result: BlockState
