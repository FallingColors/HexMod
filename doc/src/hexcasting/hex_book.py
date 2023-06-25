from dataclasses import dataclass

from minecraft.recipe import Recipe
from patchouli import Book, Page

from .hex_state import HexBookState


@dataclass
class HexBook(Book):
    """Main docgen dataclass."""

    state: HexBookState

    def __post_init__(self) -> None:
        self.state.add_stateful_unions(Page[HexBookState], Recipe[HexBookState])
        super().__post_init__()
