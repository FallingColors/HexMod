from typing import Any, Self

from pydantic import ValidationInfo, model_validator

from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.recipe import CraftingRecipe
from hexdoc.patchouli.page import PageWithText, PageWithTitle
from hexdoc.utils import ResourceLocation
from hexdoc.utils.deserialize import cast_or_raise

from ..hex_book import HexContext
from ..hex_recipes import BrainsweepRecipe
from .abstract_hex_pages import PageWithOpPattern, PageWithPattern


class LookupPatternPage(PageWithOpPattern, type="hexcasting:pattern"):
    @model_validator(mode="before")
    def _pre_root_lookup(cls, values: Any, info: ValidationInfo):
        if not info.context:
            return values
        context = cast_or_raise(info.context, HexContext)

        match values:
            case {"op_id": op_id}:
                # look up the pattern from the op id
                id = ResourceLocation.from_str(op_id)
                pattern = context.patterns[id]
                return values | {
                    "op_id": id,
                    "patterns": [pattern],
                }
            case _:
                return values

    @model_validator(mode="after")
    def _check_anchor(self) -> Self:
        if str(self.op_id) != self.anchor:
            raise ValueError(f"op_id={self.op_id} does not equal anchor={self.anchor}")
        return self


class ManualOpPatternPage(
    PageWithOpPattern,
    type="hexcasting:manual_pattern",
):
    pass


class ManualRawPatternPage(
    PageWithPattern,
    type="hexcasting:manual_pattern",
):
    pass


class ManualPatternNosigPage(
    PageWithPattern,
    type="hexcasting:manual_pattern_nosig",
    template_type="hexcasting:manual_pattern",
):
    input: None = None
    output: None = None


class CraftingMultiPage(PageWithTitle, type="hexcasting:crafting_multi"):
    heading: LocalizedStr  # TODO: should this be renamed to header?
    recipes: list[CraftingRecipe]


class BrainsweepPage(PageWithText, type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe
