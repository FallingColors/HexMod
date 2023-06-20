from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Callable, Self

from common.deserialize import TypeFn, rename
from common.formatting import FormatTree
from common.pattern import RawPatternInfo
from common.tagged_union import InternallyTaggedUnion
from common.types import Book, BookHelpers, LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import ResourceLocation


@dataclass(kw_only=True)
class BasePage(InternallyTaggedUnion, BookHelpers, tag="type", value=None):
    """Fields shared by all Page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    book: Book

    type: ResourceLocation = field(init=False)
    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    def __init_subclass__(cls, type: str | None) -> None:
        super().__init_subclass__(__class__._tag_name, type)
        if type is not None:
            cls.type = ResourceLocation.from_str(type)

    @classmethod
    def make_type_hook(cls, book: Book) -> TypeFn:
        def type_hook(data: Self | dict[str, Any]) -> Self | dict[str, Any]:
            if isinstance(data, cls):
                return data
            data = cls.assert_tag(data)
            data["book"] = book
            return data

        return type_hook


@dataclass(kw_only=True)
class PageWithText(BasePage, type=None):
    text: FormatTree | None = None


@dataclass(kw_only=True)
class PageWithTitle(PageWithText, type=None):
    _title: LocalizedStr | None = field(default=None, metadata=rename("title"))

    @property
    def title(self) -> LocalizedStr | None:
        return self._title


@dataclass(kw_only=True)
class PageWithCraftingRecipes(PageWithText, ABC, type=None):
    @property
    @abstractmethod
    def recipes(self) -> list[CraftingRecipe]:
        ...


@dataclass(kw_only=True)
class PageWithPattern(PageWithTitle, ABC, type=None):
    patterns: list[RawPatternInfo]
    op_id: ResourceLocation | None = None
    header: LocalizedStr | None = None
    input: str | None = None
    output: str | None = None
    hex_size: int | None = None

    _title: None = None

    @classmethod
    def make_type_hook(cls, book: Book) -> Callable[[dict[str, Any]], dict[str, Any]]:
        super_hook = super().make_type_hook(book)

        def type_hook(data: dict[str, Any]) -> dict[str, Any]:
            # convert a single pattern to a list
            data = super_hook(data)
            patterns = data.get("patterns")
            if patterns is not None and not isinstance(patterns, list):
                data["patterns"] = [patterns]
            return data

        return type_hook

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
