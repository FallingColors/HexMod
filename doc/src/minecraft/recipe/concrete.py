from dataclasses import dataclass

from common.state import BookState

from .abstract import Recipe
from .ingredient import ItemIngredient
from .result import ItemResult


@dataclass
class CraftingShapedRecipe(Recipe[BookState], type="minecraft:crafting_shaped"):
    pattern: list[str]
    key: dict[str, ItemIngredient]
    result: ItemResult


@dataclass
class CraftingShapelessRecipe(Recipe[BookState], type="minecraft:crafting_shapeless"):
    ingredients: list[ItemIngredient]
    result: ItemResult


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe
