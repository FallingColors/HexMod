from dataclasses import dataclass
from typing import Any, Literal

from common.dacite_patch import UnionSkip
from common.types import LocalizedItem
from minecraft.recipe import Recipe
from minecraft.recipe.concrete import CraftingRecipe
from minecraft.recipe.ingredient import ItemIngredient, ItemIngredientData
from minecraft.recipe.result import ItemResult
from minecraft.resource import ResourceLocation

from .hex_state import HexBookState

# ingredients


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


@dataclass
class ModConditionalIngredient:
    type: Literal["hexcasting:mod_conditional"]
    default: ItemIngredientData
    if_loaded: ItemIngredientData
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


@dataclass
class ModConditionalCraftingShapedRecipe(
    CraftingRecipe[HexBookState],
    type="minecraft:crafting_shaped",
):
    pattern: list[str]
    key: dict[str, ItemIngredient | ModConditionalIngredient]

    def __post_init__(self):
        if not any(
            isinstance(ingredient, ModConditionalIngredient)
            for ingredient in self.key.values()
        ):
            raise UnionSkip("Expected ModConditionalIngredient in key")


@dataclass
class ModConditionalCraftingShapelessRecipe(
    CraftingRecipe[HexBookState],
    type="minecraft:crafting_shapeless",
):
    ingredients: list[ItemIngredient | ModConditionalIngredient]
    result: ItemResult

    def __post_init__(self):
        if not any(
            isinstance(ingredient, ModConditionalIngredient)
            for ingredient in self.ingredients
        ):
            raise UnionSkip("Expected ModConditionalIngredient in ingredients")
