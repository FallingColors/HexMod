from types import NoneType
from typing import Any

from pydantic import ValidationInfo, model_validator

from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.i18n import I18nContext
from hexdoc.patchouli.page import PageWithText
from hexdoc.utils.deserialize import cast_or_raise


class PageWithPattern(PageWithText, type=None):
    header: LocalizedStr
    input: str | None = None
    output: str | None = None
    hex_size: int | None = None

    @model_validator(mode="before")
    def _pre_root_patterns(cls, values: Any):
        match values:
            case {"patterns": patterns} if not isinstance(patterns, (list, NoneType)):
                return values | {"patterns": [patterns]}
            case _:
                return values

    @property
    def args(self) -> str | None:
        inp = self.input or ""
        oup = self.output or ""
        if inp or oup:
            return f"{inp} \u2192 {oup}".strip()
        return None

    @property
    def title(self) -> str:
        suffix = f" ({self.args})" if self.args else ""
        return f"{self.header}{suffix}"


class PageWithOpPattern(PageWithPattern, type=None):
    op_id: ResourceLocation
    anchor: str

    @model_validator(mode="before")
    def _pre_root_header(cls, values: Any, info: ValidationInfo):
        if not info.context:
            return values
        context = cast_or_raise(info.context, I18nContext)

        match values:
            case {"op_id": op_id}:
                # use the pattern name as the header
                return values | {"header": context.i18n.localize_pattern(op_id)}
            case _:
                return values
