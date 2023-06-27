__all__ = [
    "Page",
    "PageWithCraftingRecipes",
    "PageWithText",
    "PageWithTitle",
    "CraftingPage",
    "EmptyPage",
    "EntityPage",
    "ImagePage",
    "LinkPage",
    "MultiblockPage",
    "QuestPage",
    "RelationsPage",
    "SmeltingPage",
    "SpotlightPage",
    "TextPage",
]

from .abstract_pages import Page, PageWithCraftingRecipes, PageWithText, PageWithTitle
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
