from abc import ABC, abstractmethod
from typing import Any, Self

from pydantic import Field, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from common.tagged_union import TypeTaggedUnion
from minecraft.i18n import LocalizedStr
from minecraft.recipe import CraftingRecipe
from minecraft.resource import ResourceLocation

from ..context import AnyBookContext
from ..formatting import FormatTree


class Page(TypeTaggedUnion[AnyBookContext], group="hexdoc.Page", type=None):
    """Base class for Patchouli page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    @model_validator(mode="wrap")
    @classmethod
    def _pre_root(cls, value: str | Any, handler: ModelWrapValidatorHandler[Self]):
        if isinstance(value, str):
            return handler({"type": "patchouli:text", "text": value})
        return handler(value)


class PageWithText(Page[AnyBookContext], type=None):
    text: FormatTree | None = None


class PageWithTitle(PageWithText[AnyBookContext], type=None):
    title_: LocalizedStr | None = Field(default=None, alias="title")

    @property
    def title(self) -> str | None:
        return self.title_.value if self.title_ else None


class PageWithCraftingRecipes(PageWithText[AnyBookContext], ABC, type=None):
    @property
    @abstractmethod
    def recipes(self) -> list[CraftingRecipe]:
        ...
