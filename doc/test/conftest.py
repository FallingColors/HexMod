import pytest
from patchouli.types import Book, FormatTree, Style


@pytest.fixture
def book() -> Book:
    return Book(
        blacklist=set(),
        categories=[],
        i18n={},
        landing_text=FormatTree(Style("", None), []),
        macros={},
        modid="",
        name="",
        pattern_reg={},
        resource_dir="",
        spoilers=set(),
        version=0,
    )
