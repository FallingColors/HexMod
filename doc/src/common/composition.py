from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING, Any

# circular imports are gross
if TYPE_CHECKING:
    from patchouli.book import Book
    from patchouli.category import Category
    from patchouli.entry import Entry
else:
    Book, Category, Entry = Any, Any, Any

# TODO: consolidate ABCs here


class WithBook(ABC):
    """ABC for composition with Book."""

    @property
    @abstractmethod
    def book(self) -> Book:
        ...

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
