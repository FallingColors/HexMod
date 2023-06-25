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

from .abstract import Page, PageWithCraftingRecipes, PageWithText, PageWithTitle
from .concrete import (
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
