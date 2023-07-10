from abc import ABC, abstractmethod
from typing import Any, cast

from pydantic import Field, ValidationInfo, model_validator

from hexcasting.pattern import RawPatternInfo
from minecraft.i18n import LocalizedStr
from minecraft.resource import ResourceLocation
from patchouli.page import PageWithTitle

from .hex_book import AnyHexContext, HexContext


# TODO: make anchor required (breaks because of Greater Sentinel)
class PageWithPattern(PageWithTitle[AnyHexContext], ABC, type=None):
    title_: None = Field(default=None, include=True)

    op_id: ResourceLocation | None = None
    header: LocalizedStr | None = None
    input: str | None = None
    output: str | None = None
    hex_size: int | None = None
    # must be after op_id, so just put it last
    patterns_: RawPatternInfo | list[RawPatternInfo] = Field(
        alias="patterns", include=True
    )

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
    def title(self) -> str:
        suffix = f" ({self.args})" if self.args else ""
        return self.name.value + suffix

    @property
    def patterns(self) -> list[RawPatternInfo]:
        if isinstance(self.patterns_, list):
            return self.patterns_
        return [self.patterns_]


class PageWithOpPattern(PageWithPattern[AnyHexContext], type=None):
    name_: LocalizedStr = Field(include=True)

    op_id: ResourceLocation
    header: None = None

    @property
    def name(self) -> LocalizedStr:
        return self.name_

    @model_validator(mode="before")
    def _check_name(cls, values: dict[str, Any], info: ValidationInfo):
        context = cast(HexContext, info.context)
        if not context or (op_id := values.get("op_id")) is None:
            return values

        name = context["i18n"].localize_pattern(op_id)
        return values | {"name_": name}


class PageWithRawPattern(PageWithPattern[AnyHexContext], type=None):
    op_id: None = None
    header: LocalizedStr

    @property
    def name(self) -> LocalizedStr:
        return self.header
