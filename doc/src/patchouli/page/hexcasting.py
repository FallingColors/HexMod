from __future__ import annotations

from dataclasses import dataclass, field

from common.deserialize import rename
from common.pattern import RawPatternInfo
from common.types import LocalizedStr
from minecraft.recipe import BrainsweepRecipe, CraftingRecipe
from minecraft.resource import ResourceLocation

from .abstract import PageWithCraftingRecipes, PageWithPattern, PageWithText


@dataclass
class PatternPage(PageWithPattern, type="hexcasting:pattern"):
    patterns: list[RawPatternInfo] = field(init=False)
    op_id: ResourceLocation
    header: None

    def __post_init__(self):
        self.patterns = [self.book.patterns[self.op_id]]

    @property
    def name(self) -> LocalizedStr:
        return self.i18n.localize_pattern(self.op_id)


@dataclass
class ManualPatternNosigPage(PageWithPattern, type="hexcasting:manual_pattern_nosig"):
    header: LocalizedStr
    op_id: None
    input: None
    output: None

    @property
    def name(self) -> LocalizedStr:
        return self.header


@dataclass
class ManualOpPatternPage(PageWithPattern, type="hexcasting:manual_pattern"):
    op_id: ResourceLocation
    header: None

    @property
    def name(self) -> LocalizedStr:
        return self.i18n.localize_pattern(self.op_id)


@dataclass
class ManualRawPatternPage(PageWithPattern, type="hexcasting:manual_pattern"):
    op_id: None
    header: LocalizedStr

    @property
    def name(self) -> LocalizedStr:
        return self.header


@dataclass
class CraftingMultiPage(PageWithCraftingRecipes, type="hexcasting:crafting_multi"):
    heading: LocalizedStr  # ...heading?
    _recipes: list[CraftingRecipe] = field(metadata=rename("recipes"))

    @property
    def recipes(self) -> list[CraftingRecipe]:
        return self._recipes


@dataclass
class BrainsweepPage(PageWithText, type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe
