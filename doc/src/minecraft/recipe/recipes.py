from common.model import HexDocModel
from minecraft.i18n import LocalizedItem
from patchouli.context import BookContext

from .abstract_recipes import Recipe
from .ingredients import ItemIngredientOrList


class ItemResult(HexDocModel[BookContext]):
    item: LocalizedItem
    count: int | None = None


class CraftingShapedRecipe(
    Recipe[BookContext],
    type="minecraft:crafting_shaped",
):
    pattern: list[str]
    key: dict[str, ItemIngredientOrList[BookContext]]
    result: ItemResult


class CraftingShapelessRecipe(
    Recipe[BookContext],
    type="minecraft:crafting_shapeless",
):
    ingredients: list[ItemIngredientOrList[BookContext]]
    result: ItemResult


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe
