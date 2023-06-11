from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass
from pathlib import Path
from typing import TYPE_CHECKING, Any

from minecraft.resource import ResourceLocation

# circular imports are gross
if TYPE_CHECKING:
    from patchouli.book import Book
    from patchouli.category import Category
    from patchouli.entry import Entry
else:
    Book, Category, Entry = Any, Any, Any


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


class Sortable(ABC):
    """ABC for classes which can be sorted."""

    @property
    @abstractmethod
    def cmp_key(self) -> Any:
        ...

    def __lt__(self, other: Any) -> bool:
        if isinstance(other, Sortable):
            return self.cmp_key < other.cmp_key
        return NotImplemented


@dataclass
class WithPathId(ABC):
    """ABC for classes with a ResourceLocation id."""

    path: Path

    @property
    @abstractmethod
    def base_dir(self) -> Path:
        """Base directory. Combine with self.id.path to find this file."""

    @property
    @abstractmethod
    def modid(self) -> str:
        ...

    @property
    def id(self) -> ResourceLocation:
        resource_path = self.path.relative_to(self.base_dir).with_suffix("").as_posix()
        return ResourceLocation(self.modid, resource_path)

    @property
    def href(self) -> str:
        return f"#{self.id.path}"
