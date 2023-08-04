__all__ = [
    "Page",
    "PageWithText",
    "PageWithTitle",
    "TextPage",
    "ImagePage",
    "CraftingPage",
    "SmeltingPage",
    "MultiblockPage",
    "EntityPage",
    "SpotlightPage",
    "LinkPage",
    "RelationsPage",
    "QuestPage",
    "EmptyPage",
]

from .abstract_pages import Page, PageWithText, PageWithTitle
from .pages import (
    CraftingPage,
    EmptyPage,
    EntityPage,
    ImagePage,
    LinkPage,
    MultiblockPage,
    QuestPage,
    RelationsPage,
    SmeltingPage,
    SpotlightPage,
    TextPage,
)
