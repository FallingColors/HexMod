from __future__ import annotations

from dataclasses import dataclass
from typing import TYPE_CHECKING, Any

# circular imports are gross
if TYPE_CHECKING:
    from patchouli.book import Book
    from patchouli.category import Category
    from patchouli.entry import Entry
else:
    Book, Category, Entry = Any, Any, Any


@dataclass
class WithBook:
    """Helper base class for composition with Book."""

    book: Book

    @property
    def resource_dir(self):
        """book.resource_dir"""
        return self.book.resource_dir

    @property
    def modid(self):
        """book.modid"""
        return self.book.modid

    @property
    def i18n(self):
        """book.i18n"""
        return self.book.i18n
