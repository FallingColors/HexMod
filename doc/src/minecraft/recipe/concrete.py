from dataclasses import dataclass

from .abstract import BaseRecipe
from .ingredient import BlockStateIngredient, ItemIngredient, VillagerIngredient
from .result import BlockState, ItemResult


@dataclass
class CraftingShapedRecipe(BaseRecipe, type="minecraft:crafting_shaped"):
    pattern: list[str]
    key: dict[str, ItemIngredient]
    result: ItemResult


@dataclass
class CraftingShapelessRecipe(BaseRecipe, type="minecraft:crafting_shapeless"):
    ingredients: list[ItemIngredient]
    result: ItemResult


@dataclass
class BrainsweepRecipe(BaseRecipe, type="hexcasting:brainsweep"):
    blockIn: BlockStateIngredient
    villagerIn: VillagerIngredient
    result: BlockState


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe

Recipe = CraftingShapedRecipe | CraftingShapelessRecipe | BrainsweepRecipe
