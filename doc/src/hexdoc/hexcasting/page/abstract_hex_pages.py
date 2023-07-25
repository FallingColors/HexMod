from types import NoneType
from typing import Any, cast

from pydantic import ValidationInfo, model_validator

from hexdoc.minecraft import LocalizedStr
from hexdoc.patchouli.page import PageWithText
from hexdoc.resource import ResourceLocation

from ..hex_book import AnyHexContext, HexContext
from ..pattern import RawPatternInfo


# TODO: make anchor required (breaks because of Greater Sentinel)
class PageWithPattern(PageWithText[AnyHexContext], type=None):
    header: LocalizedStr
    patterns: list[RawPatternInfo]
    input: str | None = None
    output: str | None = None
    hex_size: int | None = None

    @model_validator(mode="before")
    def _pre_root_patterns(cls, values: dict[str, Any]):
        # patterns may be a list or a single pattern, so make sure we always get a list
        patterns = values.get("patterns")
        if isinstance(patterns, (NoneType, list)):
            return values
        return values | {"patterns": [patterns]}

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


class PageWithOpPattern(PageWithPattern[AnyHexContext], type=None):
    op_id: ResourceLocation

    @model_validator(mode="before")
    def _pre_root_header(cls, values: dict[str, Any], info: ValidationInfo):
        context = cast(HexContext, info.context)
        if not context:
            return values

        # use the pattern name as the header
        return values | {
            "header": context["i18n"].localize_pattern(values["op_id"]),
        }
