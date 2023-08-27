from hexdoc.utils import HexdocModel

from ..i18n import LocalizedItem
from .abstract_recipes import Recipe
from .ingredients import ItemIngredientOrList


class ItemResult(HexdocModel):
    item: LocalizedItem
    count: int | None = None


class CraftingShapedRecipe(Recipe, type="minecraft:crafting_shaped"):
    key: dict[str, ItemIngredientOrList]
    pattern: list[str]
    result: ItemResult
    show_notification: bool


class CraftingShapelessRecipe(Recipe, type="minecraft:crafting_shapeless"):
    ingredients: list[ItemIngredientOrList]
    result: ItemResult


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe
