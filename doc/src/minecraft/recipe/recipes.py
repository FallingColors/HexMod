from dataclasses import dataclass

from patchouli.state import BookState

from .abstract_recipes import ItemResult, Recipe
from .ingredients import ItemIngredientOrList


@dataclass
class CraftingShapedRecipe(
    Recipe[BookState],
    type="minecraft:crafting_shaped",
):
    pattern: list[str]
    key: dict[str, ItemIngredientOrList[BookState]]
    result: ItemResult


@dataclass
class CraftingShapelessRecipe(
    Recipe[BookState],
    type="minecraft:crafting_shapeless",
):
    ingredients: list[ItemIngredientOrList[BookState]]
    result: ItemResult


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe
