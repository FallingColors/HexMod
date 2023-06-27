from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field

from common.deserialize import rename
from common.pattern import RawPatternInfo
from common.types import LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import ResourceLocation
from patchouli.page import PageWithCraftingRecipes, PageWithText, PageWithTitle

from .hex_recipes import BrainsweepRecipe
from .hex_state import HexBookState


@dataclass(kw_only=True)
class PageWithPattern(PageWithTitle[HexBookState], ABC, type=None):
    _patterns: RawPatternInfo | list[RawPatternInfo] = field(
        metadata=rename("patterns")
    )
    op_id: ResourceLocation | None = None
    header: LocalizedStr | None = None
    input: str | None = None
    output: str | None = None
    hex_size: int | None = None

    _title: None = None

    @property
    @abstractmethod
    def name(self) -> LocalizedStr:
        ...

    @property
    def args(self) -> str | None:
        inp = self.input or ""
        oup = self.output or ""
        if inp or oup:
            return f"{inp} \u2192 {oup}".strip()
        return None

    @property
    def title(self) -> LocalizedStr:
        suffix = f" ({self.args})" if self.args else ""
        return LocalizedStr(self.name + suffix)

    @property
    def patterns(self) -> list[RawPatternInfo]:
        if isinstance(self._patterns, list):
            return self._patterns
        return [self._patterns]


@dataclass
class LookupPatternPage(PageWithPattern, type="hexcasting:pattern"):
    state: HexBookState

    _patterns: list[RawPatternInfo] = field(init=False)
    op_id: ResourceLocation
    header: None

    def __post_init__(self):
        self._patterns = [self.state.patterns[self.op_id]]

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
class CraftingMultiPage(
    PageWithCraftingRecipes[HexBookState],
    type="hexcasting:crafting_multi",
):
    heading: LocalizedStr  # ...heading?
    _recipes: list[CraftingRecipe] = field(metadata=rename("recipes"))

    @property
    def recipes(self) -> list[CraftingRecipe]:
        return self._recipes


@dataclass
class BrainsweepPage(PageWithText[HexBookState], type="hexcasting:brainsweep"):
    recipe: BrainsweepRecipe
