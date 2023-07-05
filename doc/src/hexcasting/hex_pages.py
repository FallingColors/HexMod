from typing import Any, cast

from pydantic import Field, ValidationInfo, model_validator

from hexcasting.pattern import RawPatternInfo
from minecraft.i18n import LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import ResourceLocation
from patchouli.page import PageWithCraftingRecipes, PageWithText

from .abstract_hex_pages import PageWithOpPattern, PageWithRawPattern
from .hex_book import HexContext
from .hex_recipes import BrainsweepRecipe


class LookupPatternPage(
    PageWithOpPattern[HexContext],
    type="hexcasting:pattern",
):
    patterns_: list[RawPatternInfo]

    @model_validator(mode="before")
    def _check_patterns(cls, data: dict[str, Any], info: ValidationInfo):
        context = cast(HexContext, info.context)
        if not context:
            return data

        # look up the pattern from the op id
        op_id = ResourceLocation.from_str(data["op_id"])
        pattern = context["patterns"][op_id]
        return data | {"patterns_": [pattern], "op_id": op_id}


class ManualOpPatternPage(
    PageWithOpPattern[HexContext],
    type="hexcasting:manual_pattern",
):
    pass


class ManualRawPatternPage(
    PageWithRawPattern[HexContext],
    type="hexcasting:manual_pattern",
):
    pass


class ManualPatternNosigPage(
    PageWithRawPattern[HexContext],
    type="hexcasting:manual_pattern_nosig",
):
    input: None = None
    output: None = None


class CraftingMultiPage(
    PageWithCraftingRecipes[HexContext],
    type="hexcasting:crafting_multi",
):
    heading: LocalizedStr  # ...heading?
    recipes_: list[CraftingRecipe] = Field(alias="recipes", include=True)

    @property
    def recipes(self) -> list[CraftingRecipe]:
        return self.recipes_


class BrainsweepPage(PageWithText[HexContext], type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe
