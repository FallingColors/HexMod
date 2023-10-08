from typing import Iterator

from pydantic import field_validator

from hexdoc.utils.compat import HexVersion

from .abstract_recipes import CraftingRecipe
from .ingredients import ItemIngredientList


class CraftingShapelessRecipe(CraftingRecipe, type="minecraft:crafting_shapeless"):
    ingredients: list[ItemIngredientList]


class CraftingShapedRecipe(CraftingRecipe, type="minecraft:crafting_shaped"):
    key: dict[str, ItemIngredientList]
    pattern: list[str]
    show_notification: bool | None = None

    @property
    def ingredients(self) -> Iterator[ItemIngredientList | None]:
        for row in self.pattern:
            if len(row) > 3:
                raise ValueError(f"Expected len(row) <= 3, got {len(row)}: `{row}`")
            for item_key in row.ljust(3):
                match item_key:
                    case " ":
                        yield None
                    case _:
                        yield self.key[item_key]

    @field_validator("show_notification")
    @classmethod
    def _check_show_notification(cls, value: bool | None):
        match HexVersion.get():
            case HexVersion.v0_11_x:
                HexVersion.check(value is not None, "show_notification")
            case HexVersion.v0_10_x | HexVersion.v0_9_x:
                HexVersion.check(value is None, "show_notification")
        return value
