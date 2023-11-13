from typing import Any, Literal

from hexdoc.core import IsVersion, ResourceLocation
from hexdoc.minecraft.assets import ItemWithTexture
from hexdoc.minecraft.recipe import ItemIngredient, ItemIngredientList, Recipe
from hexdoc.model import HexdocModel, TypeTaggedUnion
from hexdoc.utils import NoValue

# ingredients


class BrainsweepeeIngredient(TypeTaggedUnion, type=None):
    pass


# lol, lmao
class VillagerIngredient(BrainsweepeeIngredient, type="villager"):
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


class VillagerIngredient_0_10(VillagerIngredient, type=NoValue):
    pass


@IsVersion(">=1.20")
class EntityTypeIngredient(BrainsweepeeIngredient, type="entity_type"):
    entityType: ResourceLocation


@IsVersion(">=1.20")
class EntityTagIngredient(BrainsweepeeIngredient, type="entity_tag"):
    tag: ResourceLocation


class BlockStateIngredient(HexdocModel):
    # TODO: tagged union
    type: Literal["block"]
    block: ItemWithTexture


class ModConditionalIngredient(ItemIngredient, type="hexcasting:mod_conditional"):
    default: ItemIngredientList
    if_loaded: ItemIngredientList
    modid: str


# results


class BlockState(HexdocModel):
    name: ItemWithTexture
    properties: dict[str, Any] | None = None


# recipes


class BrainsweepRecipe(Recipe, type=None):
    blockIn: BlockStateIngredient
    result: BlockState


@IsVersion(">=1.20")
class BrainsweepRecipe_0_11(BrainsweepRecipe, type="hexcasting:brainsweep"):
    cost: int
    entityIn: BrainsweepeeIngredient


@IsVersion("<=1.19")
class BrainsweepRecipe_0_10(BrainsweepRecipe, type="hexcasting:brainsweep"):
    villagerIn: VillagerIngredient_0_10
