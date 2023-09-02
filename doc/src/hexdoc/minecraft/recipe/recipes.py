from pydantic import field_validator

from hexdoc.utils import HexdocModel
from hexdoc.utils.compat import HexVersion

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
    show_notification: bool | None = None

    @field_validator("show_notification")
    @classmethod
    def _check_show_notification(cls, value: bool | None):
        match HexVersion.get():
            case HexVersion.v0_11:
                HexVersion.check(value is not None, "show_notification")
            case HexVersion.v0_10:
                HexVersion.check(value is None, "show_notification")
        return value


class CraftingShapelessRecipe(Recipe, type="minecraft:crafting_shapeless"):
    ingredients: list[ItemIngredientOrList]
    result: ItemResult


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe
