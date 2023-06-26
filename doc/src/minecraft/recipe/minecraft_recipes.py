from dataclasses import dataclass

from common.state import BookState

from .abstract_recipes import CraftingRecipe, ItemIngredient


@dataclass
class CraftingShapedRecipe(
    CraftingRecipe[BookState],
    type="minecraft:crafting_shaped",
):
    pattern: list[str]
    key: dict[str, ItemIngredient]


@dataclass
class CraftingShapelessRecipe(
    CraftingRecipe[BookState],
    type="minecraft:crafting_shapeless",
):
    ingredients: list[ItemIngredient]
