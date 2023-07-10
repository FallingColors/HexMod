# pyright: reportPrivateUsage=false

from __future__ import annotations

import re
from abc import ABC, abstractmethod
from contextlib import nullcontext
from enum import Enum, auto
from typing import Any, Literal, Self, cast

from pydantic import ValidationInfo, model_validator
from pydantic.dataclasses import dataclass
from pydantic.functional_validators import ModelWrapValidatorHandler

from common.model import DEFAULT_CONFIG, HexDocModel
from common.properties import Properties
from common.types import TryGetEnum
from minecraft.i18n import I18nContext, LocalizedStr

from .html import HTMLElement, HTMLStream

DEFAULT_MACROS = {
    "$(obf)": "$(k)",
    "$(bold)": "$(l)",
    "$(strike)": "$(m)",
    "$(italic)": "$(o)",
    "$(italics)": "$(o)",
    "$(list": "$(li",
    "$(reset)": "$()",
    "$(clear)": "$()",
    "$(2br)": "$(br2)",
    "$(p)": "$(br2)",
    "/$": "$()",
    "<br>": "$(br)",
    "$(nocolor)": "$(0)",
    "$(item)": "$(#b0b)",
    "$(thing)": "$(#490)",
}

_REPLACEMENTS = {
    "br": "\n",
    "playername": "[Playername]",
}

_COLORS = {
    "0": "000",
    "1": "00a",
    "2": "0a0",
    "3": "0aa",
    "4": "a00",
    "5": "a0a",
    "6": "fa0",
    "7": "aaa",
    "8": "555",
    "9": "55f",
    "a": "5f5",
    "b": "5ff",
    "c": "f55",
    "d": "f5f",
    "e": "ff5",
    "f": "fff",
}

_KEYS = {
    "use": "Right Click",
    "sneak": "Left Shift",
}


# Higgledy piggledy
# Old fuck Alwinfy said,
# "Eschew your typechecks and
# live with a pair,"
#
# Making poor Object do
# Re-re-re-factoring
# Till Winfy took up her
# Classical flair.


class CommandStyleType(TryGetEnum):
    """Command styles, like `$(type)`."""

    obfuscated = "k"
    bold = "l"
    strikethrough = "m"
    underline = "n"
    italic = "o"


class FunctionStyleType(TryGetEnum):
    """Function styles, like `$(type:value)`."""

    link = "l"
    tooltip = "t"
    cmd_click = "c"


class SpecialStyleType(Enum):
    """Styles with no defined name, like `$(#0080ff)`, or styles which must be handled
    differently than the normal styles, like `$()`."""

    base = auto()
    paragraph = auto()
    color = auto()


BaseStyleType = Literal[SpecialStyleType.base]

ParagraphStyleType = Literal[SpecialStyleType.paragraph]

ColorStyleType = Literal[SpecialStyleType.color]


class Style(ABC, HexDocModel[Any], frozen=True):
    type: CommandStyleType | FunctionStyleType | SpecialStyleType

    @staticmethod
    def parse(style_str: str, is_0_black: bool) -> Style | _CloseTag | str:
        # direct text replacements
        if style_str in _REPLACEMENTS:
            return _REPLACEMENTS[style_str]

        # paragraph
        if style := ParagraphStyle.try_parse(style_str):
            return style

        # commands
        if style_type := CommandStyleType.get(style_str):
            return CommandStyle(type=style_type)

        # reset color, but only if 0 is considered reset instead of black
        if not is_0_black and style_str == "0":
            return _CloseTag(type=SpecialStyleType.color)

        # preset colors
        if style_str in _COLORS:
            return FunctionStyle(type=SpecialStyleType.color, value=_COLORS[style_str])

        # hex colors (#rgb and #rrggbb)
        if style_str.startswith("#") and len(style_str) in [4, 7]:
            return FunctionStyle(type=SpecialStyleType.color, value=style_str[1:])

        # functions
        if ":" in style_str:
            name, value = style_str.split(":", 1)

            # keys
            if name == "k":
                if value in _KEYS:
                    return _KEYS[value]

            # all the other functions
            if style_type := FunctionStyleType.get(name):
                return FunctionStyle(type=style_type, value=value)

        # reset
        if style_str == "":
            return _CloseTag(type=SpecialStyleType.base)

        # close functions
        if style_str.startswith("/"):
            if style_type := FunctionStyleType.get(style_str[1:]):
                return _CloseTag(type=style_type)

        # oopsies
        raise ValueError(f"Unhandled style: {style_str}")

    @abstractmethod
    def element(self, out: HTMLStream) -> HTMLElement | nullcontext[None]:
        ...


class CommandStyle(Style, frozen=True):
    type: CommandStyleType | BaseStyleType

    def element(self, out: HTMLStream) -> HTMLElement | nullcontext[None]:
        match self.type:
            case CommandStyleType.obfuscated:
                return out.element("span", class_name="obfuscated")
            case CommandStyleType.bold:
                return out.element("strong")
            case CommandStyleType.strikethrough:
                return out.element("s")
            case CommandStyleType.underline:
                return out.element("span", style="text-decoration: underline")
            case CommandStyleType.italic:
                return out.element("i")
            case SpecialStyleType.base:
                return nullcontext()


class ParagraphStyle(Style, frozen=True):
    type: ParagraphStyleType = SpecialStyleType.paragraph
    attributes: dict[str, str]

    @classmethod
    def try_parse(cls, style_str: str) -> Self | None:
        match style_str:
            case "br2":
                return cls.paragraph()
            case "li":
                return cls.list_item()
            case _:
                pass

    @classmethod
    def paragraph(cls) -> Self:
        return cls(attributes={})

    @classmethod
    def list_item(cls) -> Self:
        return cls(attributes={"class_name": "fake-li"})

    def element(self, out: HTMLStream) -> HTMLElement:
        return out.element("p", **self.attributes)


def _format_href(value: str) -> str:
    if not value.startswith(("http:", "https:")):
        return "#" + value.replace("#", "@")
    return value


class FunctionStyle(Style, frozen=True):
    type: FunctionStyleType | ColorStyleType
    value: str

    def element(self, out: HTMLStream) -> HTMLElement:
        match self.type:
            case FunctionStyleType.link:
                return out.element("a", href=_format_href(self.value))
            case FunctionStyleType.tooltip:
                return out.element("span", class_name="has-tooltip", title=self.value)
            case FunctionStyleType.cmd_click:
                return out.element(
                    "span",
                    class_name="has-cmd_click",
                    title=f"When clicked, would execute: {self.value}",
                )
            case SpecialStyleType.color:
                return out.element("span", style=f"color: #{self.value}")


# intentionally not inheriting from Style, because this is basically an implementation
# detail of the parser and should not be returned or exposed anywhere
class _CloseTag(HexDocModel[Any], frozen=True):
    type: FunctionStyleType | BaseStyleType | ColorStyleType


_FORMAT_RE = re.compile(r"\$\(([^)]*)\)")


class FormatContext(I18nContext):
    macros: dict[str, str]
    props: Properties


@dataclass(config=DEFAULT_CONFIG)
class FormatTree:
    style: Style
    children: list[FormatTree | str]  # this can't be Self, it breaks Pydantic

    @classmethod
    def format(cls, string: str, macros: dict[str, str], is_0_black: bool) -> Self:
        # resolve macros
        # TODO: use ahocorasick? this feels inefficient
        old_string = None
        while old_string != string:
            old_string = string
            for macro, replace in macros.items():
                string = string.replace(macro, replace)

        # lex out parsed styles
        text_nodes: list[str] = []
        styles: list[Style | _CloseTag] = []
        text_since_prev_style: list[str] = []
        last_end = 0

        for match in re.finditer(_FORMAT_RE, string):
            # get the text between the previous match and here
            leading_text = string[last_end : match.start()]
            text_since_prev_style.append(leading_text)
            last_end = match.end()

            match Style.parse(match[1], is_0_black):
                case str(replacement):
                    # str means "use this instead of the original value"
                    text_since_prev_style.append(replacement)
                case Style() | _CloseTag() as style:
                    # add this style and collect the text since the previous one
                    styles.append(style)
                    text_nodes.append("".join(text_since_prev_style))
                    text_since_prev_style.clear()

        text_nodes.append("".join(text_since_prev_style) + string[last_end:])
        first_node = text_nodes.pop(0)

        # parse
        style_stack = [
            FormatTree(CommandStyle(type=SpecialStyleType.base), []),
            FormatTree(ParagraphStyle.paragraph(), [first_node]),
        ]
        for style, text in zip(styles, text_nodes):
            tmp_stylestack: list[Style] = []
            if style.type == SpecialStyleType.base:
                while style_stack[-1].style.type != SpecialStyleType.paragraph:
                    last_node = style_stack.pop()
                    style_stack[-1].children.append(last_node)
            elif any(tree.style.type == style.type for tree in style_stack):
                while len(style_stack) >= 2:
                    last_node = style_stack.pop()
                    style_stack[-1].children.append(last_node)
                    if last_node.style.type == style.type:
                        break
                    tmp_stylestack.append(last_node.style)

            for sty in tmp_stylestack:
                style_stack.append(FormatTree(sty, []))

            if isinstance(style, _CloseTag):
                if text:
                    style_stack[-1].children.append(text)
            else:
                style_stack.append(FormatTree(style, [text] if text else []))

        while len(style_stack) >= 2:
            last_node = style_stack.pop()
            style_stack[-1].children.append(last_node)

        return style_stack[0]

    @model_validator(mode="wrap")
    @classmethod
    def _wrap_root(
        cls,
        value: str | LocalizedStr | Self,
        handler: ModelWrapValidatorHandler[Self],
        info: ValidationInfo,
    ):
        context = cast(FormatContext, info.context)
        if not context or isinstance(value, FormatTree):
            return handler(value)

        if not isinstance(value, LocalizedStr):
            value = context["i18n"].localize(value)
        return cls.format(value.value, context["macros"], context["props"].is_0_black)
