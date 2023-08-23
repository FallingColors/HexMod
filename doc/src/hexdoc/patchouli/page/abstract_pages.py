from typing import Any, ClassVar, Self

from pydantic import model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.minecraft import LocalizedStr
from hexdoc.utils import ResourceLocation, TypeTaggedUnion

from ..text import FormatTree


class Page(TypeTaggedUnion, group="hexdoc.Page", type=None):
    """Base class for Patchouli page types.

    See: https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/page-types
    """

    __template: ClassVar[str]

    type: ResourceLocation | None
    advancement: ResourceLocation | None = None
    flag: str | None = None
    anchor: str | None = None

    def __init_subclass__(
        cls,
        *,
        type: str | None,
        template_type: str | None = None,
    ) -> None:
        super().__init_subclass__(group=None, type=type)

        # jinja template path
        if template_type is not None:
            template = ResourceLocation.from_str(template_type)
        else:
            template = cls.type

        if template:
            cls.__template = f"pages/{template.namespace}/{template.path}.html.jinja"

    @model_validator(mode="wrap")
    @classmethod
    def _pre_root(cls, value: str | Any, handler: ModelWrapValidatorHandler[Self]):
        if isinstance(value, str):
            return handler({"type": "patchouli:text", "text": value})
        return handler(value)

    @property
    def template(self) -> str:
        return self.__template


class PageWithText(Page, type=None):
    text: FormatTree | None = None


class PageWithTitle(PageWithText, type=None):
    title: LocalizedStr | None = None
