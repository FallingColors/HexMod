from dataclasses import dataclass
from typing import Any

from common.deserialize import TypeHooks
from common.properties import Properties
from common.state import StatefulUnions
from minecraft.i18n import I18n
from minecraft.recipe import Recipe
from patchouli import Book, Page

from .hex_pages import (
    BrainsweepPage,
    CraftingMultiPage,
    LookupPatternPage,
    ManualOpPatternPage,
    ManualPatternNosigPage,
    ManualRawPatternPage,
)
from .hex_recipes import BrainsweepRecipe
from .hex_state import HexBookState

_HEX_PAGES = [
    LookupPatternPage,
    ManualPatternNosigPage,
    ManualOpPatternPage,
    ManualRawPatternPage,
    CraftingMultiPage,
    BrainsweepPage,
]

_HEX_RECIPES = [
    BrainsweepRecipe,
]


@dataclass
class HexBook(Book[HexBookState]):
    """Main docgen dataclass."""

    @classmethod
    def _init_state(
        cls,
        data: dict[str, Any],
        props: Properties,
        i18n: I18n,
        macros: dict[str, str],
        type_hooks: TypeHooks[Any],
        stateful_unions: StatefulUnions[HexBookState],
    ) -> HexBookState:
        stateful_unions = dict(stateful_unions) | {
            Page[HexBookState]: _HEX_PAGES,
            Recipe[HexBookState]: _HEX_RECIPES,
        }
        return HexBookState(props, i18n, macros, type_hooks, stateful_unions)
