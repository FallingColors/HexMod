from typing import Any, ClassVar, Self

from pydantic import model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.minecraft import LocalizedStr
from hexdoc.resource import ResourceLocation, TypeTaggedUnion
from hexdoc.utils import TagValue

from ..model import AnyBookContext
from ..text import FormatTree


class Page(TypeTaggedUnion[AnyBookContext], group="hexdoc.Page", type=None):
    """Base class for Patchouli page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    __template: ClassVar[str]

    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    def __init_subclass__(
        cls,
        *,
        type: TagValue | None,
        template: str | None = None,
        template_name: str | None = None,
    ) -> None:
        super().__init_subclass__(group=None, type=type)

        # jinja template
        match template, template_name:
            case str(), None:
                cls.__template = template
            case None, str():
                cls.__template = f"pages/{template_name}.html.jinja"
            case None, None:
                cls.__template = f"pages/{cls.__name__}.html.jinja"
            case _:
                raise ValueError("Must specify at most one of template, template_name")

    @model_validator(mode="wrap")
    @classmethod
    def _pre_root(cls, value: str | Any, handler: ModelWrapValidatorHandler[Self]):
        if isinstance(value, str):
            return handler({"type": "patchouli:text", "text": value})
        return handler(value)

    @property
    def template(self) -> str:
        return self.__template


class PageWithText(Page[AnyBookContext], type=None):
    text: FormatTree | None = None


class PageWithTitle(PageWithText[AnyBookContext], type=None):
    title: LocalizedStr | None = None
