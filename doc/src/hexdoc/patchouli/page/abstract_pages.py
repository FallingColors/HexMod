from typing import Any, ClassVar, Self, Unpack

from pydantic import ConfigDict, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import LocalizedStr
from hexdoc.model.tagged_union import TypeTaggedUnion
from hexdoc.utils.classproperty import classproperty
from hexdoc.utils.singletons import Inherit, InheritType, NoValue

from ..text import FormatTree


class Page(TypeTaggedUnion, type=None):
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
        type: str | InheritType | None = Inherit,
        template_type: str | None = None,
        **kwargs: Unpack[ConfigDict],
    ) -> None:
        super().__init_subclass__(type=type, **kwargs)

        # jinja template path
        if template_type is not None:
            template_id = ResourceLocation.from_str(template_type)
        else:
            template_id = cls.type

        if template_id:
            cls.__template = f"pages/{template_id.namespace}/{template_id.path}"

    @classproperty
    @classmethod
    def type(cls) -> ResourceLocation | None:
        assert cls._type is not NoValue
        return cls._type

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
    """Base class for a `Page` with optional text.

    If text is required, do not subclass this type.
    """

    text: FormatTree | None = None


class PageWithTitle(PageWithText, type=None):
    """Base class for a `Page` with optional title and text.

    If title and/or text is required, do not subclass this type.
    """

    title: LocalizedStr | None = None
