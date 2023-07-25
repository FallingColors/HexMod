from typing import Any, cast

from pydantic import ValidationInfo, model_validator

from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.recipe import CraftingRecipe
from hexdoc.patchouli.page import PageWithText, PageWithTitle
from hexdoc.resource import ResourceLocation

from ..hex_book import HexContext
from ..hex_recipes import BrainsweepRecipe
from .abstract_hex_pages import PageWithOpPattern, PageWithPattern


class LookupPatternPage(
    PageWithOpPattern[HexContext],
    type="hexcasting:pattern",
):
    @model_validator(mode="before")
    def _pre_root_lookup(cls, values: dict[str, Any], info: ValidationInfo):
        context = cast(HexContext, info.context)
        if not context:
            return values

        # look up the pattern from the op id
        op_id = ResourceLocation.from_str(values["op_id"])
        pattern = context["patterns"][op_id]
        return values | {
            "op_id": op_id,
            "patterns": [pattern],
        }


class ManualOpPatternPage(
    PageWithOpPattern[HexContext],
    type="hexcasting:manual_pattern",
    template_name="PageWithPattern",
):
    pass


class ManualRawPatternPage(
    PageWithPattern[HexContext],
    type="hexcasting:manual_pattern",
    template_name="PageWithPattern",
):
    pass


class ManualPatternNosigPage(
    PageWithPattern[HexContext],
    type="hexcasting:manual_pattern_nosig",
    template_name="PageWithPattern",
):
    input: None = None
    output: None = None


class CraftingMultiPage(PageWithTitle[HexContext], type="hexcasting:crafting_multi"):
    heading: LocalizedStr  # TODO: should this be renamed to header?
    recipes: list[CraftingRecipe]


class BrainsweepPage(PageWithText[HexContext], type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe
