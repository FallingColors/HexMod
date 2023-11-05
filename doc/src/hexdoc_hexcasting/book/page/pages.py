from typing import Any, Self

from hexdoc_hexcasting.metadata import HexContext
from hexdoc_hexcasting.utils.pattern import PatternInfo, RawPatternInfo
from pydantic import ValidationInfo, field_validator, model_validator

from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.recipe import CraftingRecipe
from hexdoc.patchouli import BookContext
from hexdoc.patchouli.page import PageWithText, PageWithTitle
from hexdoc.utils import cast_or_raise

from ..recipes import BrainsweepRecipe
from .abstract_pages import PageWithOpPattern, PageWithPattern


class LookupPatternPage(PageWithOpPattern, type="hexcasting:pattern"):
    @property
    def patterns(self) -> list[PatternInfo]:
        return self._patterns

    @model_validator(mode="after")
    def _post_root_lookup(self, info: ValidationInfo):
        context = cast_or_raise(info.context, BookContext)
        hex_context = cast_or_raise(context.extra["hexcasting"], HexContext)
        self._patterns = [hex_context.patterns[self.op_id]]
        return self

    @model_validator(mode="after")
    def _check_anchor(self) -> Self:
        if str(self.op_id) != self.anchor:
            raise ValueError(f"op_id={self.op_id} does not equal anchor={self.anchor}")
        return self


class ManualOpPatternPage(
    PageWithOpPattern,
    type="hexcasting:manual_pattern",
):
    patterns: list[RawPatternInfo]


class ManualRawPatternPage(
    PageWithPattern,
    type="hexcasting:manual_pattern",
):
    patterns: list[RawPatternInfo]


class ManualPatternNosigPage(
    PageWithPattern,
    type="hexcasting:manual_pattern_nosig",
    template_type="hexcasting:manual_pattern",
):
    patterns: list[RawPatternInfo]

    @field_validator("input", "output")
    def _forbid_arguments(cls, value: Any):
        assert value is None


class CraftingMultiPage(PageWithTitle, type="hexcasting:crafting_multi"):
    heading: LocalizedStr  # TODO: should this be renamed to header?
    recipes: list[CraftingRecipe]

    @model_validator(mode="after")
    def _set_title(self):
        self.title = self.heading
        return self


class BrainsweepPage(PageWithText, type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe
