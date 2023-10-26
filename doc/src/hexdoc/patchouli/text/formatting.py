# pyright: reportPrivateUsage=false

from __future__ import annotations

import re
from enum import Enum, auto
from typing import Any, Literal, Self

from jinja2 import pass_context
from jinja2.runtime import Context
from pydantic import Field, ValidationInfo, model_validator
from pydantic.dataclasses import dataclass
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.core.loader import LoaderContext
from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import LocalizedStr
from hexdoc.minecraft.i18n import I18n, I18nContext
from hexdoc.model import DEFAULT_CONFIG, HexdocModel
from hexdoc.utils.classproperty import classproperty
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.types import TryGetEnum

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

BookLinkBases = dict[tuple[ResourceLocation, str | None], str]


class FormattingContext(
    I18nContext,
    LoaderContext,
    arbitrary_types_allowed=True,
):
    book_id: ResourceLocation
    macros: dict[str, str] = Field(default_factory=dict)

    @model_validator(mode="after")
    def _add_macros(self, info: ValidationInfo) -> Self:
        # precedence: ctx arguments, book macros, default macros
        context: dict[str, Any] = cast_or_raise(info.context, dict)
        self.macros = DEFAULT_MACROS | context.get("macros", {}) | self.macros
        return self


class BookLink(HexdocModel):
    raw_value: str
    id: ResourceLocation
    anchor: str | None

    @classmethod
    def from_str(cls, raw_value: str, book_id: ResourceLocation) -> Self:
        # anchor
        if "#" in raw_value:
            id_str, anchor = raw_value.split("#", 1)
        else:
            id_str, anchor = raw_value, None

        # id of category or entry being linked to
        if ":" in id_str:
            id = ResourceLocation.from_str(id_str)
            # eg. link to patterns/spells/links instead of hexal:patterns/spells/links
            raw_value = raw_value.removeprefix(f"{id.namespace}:")
        else:
            id = book_id.with_path(id_str)

        return cls(raw_value=raw_value, id=id, anchor=anchor)

    @property
    def as_tuple(self) -> tuple[ResourceLocation, str | None]:
        return (self.id, self.anchor)

    @property
    def fragment(self) -> str:
        return f"#{self.raw_value.replace('#', '@')}"


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

    @classproperty
    @classmethod
    def macro_group(cls) -> str:
        return "command"


class FunctionStyleType(TryGetEnum):
    """Function styles, like `$(type:value)`."""

    tooltip = "t"
    cmd_click = "c"

    @classproperty
    @classmethod
    def macro_group(cls) -> str:
        return "function"


class SpecialStyleType(Enum):
    """Styles with no defined name, like `$(#0080ff)`, or styles which must be handled
    differently than the normal styles, like `$()`."""

    base = auto()
    paragraph = auto()
    color = auto()
    link = "l"

    @classproperty
    @classmethod
    def macro_group(cls) -> str:
        return "special"


class Style(HexdocModel, frozen=True):
    type: CommandStyleType | FunctionStyleType | SpecialStyleType

    @staticmethod
    def parse(
        style_str: str,
        book_id: ResourceLocation,
        i18n: I18n,
        is_0_black: bool,
    ) -> Style | _CloseTag | str:
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
        if style_str == "0" and not is_0_black:
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
                return str(i18n.localize_key(value))

            # links
            if name == SpecialStyleType.link.value:
                return LinkStyle(value=_format_href(value, book_id))

            # all the other functions
            if style_type := FunctionStyleType.get(name):
                return FunctionStyle(type=style_type, value=value)

        # reset
        if style_str == "":
            return _CloseTag(type=SpecialStyleType.base)

        # close functions
        if style_str.startswith("/"):
            # links
            if style_str[1:] == SpecialStyleType.link.value:
                return _CloseTag(type=SpecialStyleType.link)

            # all the other functions
            if style_type := FunctionStyleType.get(style_str[1:]):
                return _CloseTag(type=style_type)

        # oopsies
        raise ValueError(f"Unhandled style: {style_str}")

    @property
    def macro(self) -> str:
        return f"{self.type.macro_group}_{self.type.name}"


def is_external_link(value: str) -> bool:
    return value.startswith(("https:", "http:"))


def _format_href(value: str, book_id: ResourceLocation) -> str | BookLink:
    # TODO: kinda hacky, BookLink should *probably* support query params
    if value.startswith("?") or is_external_link(value):
        return value
    return BookLink.from_str(value, book_id)


class CommandStyle(Style, frozen=True):
    type: CommandStyleType | Literal[SpecialStyleType.base]


class ParagraphStyleSubtype(Enum):
    paragraph = auto()
    list_item = auto()


class ParagraphStyle(Style, frozen=True):
    type: Literal[SpecialStyleType.paragraph] = SpecialStyleType.paragraph
    subtype: ParagraphStyleSubtype

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
        return cls(subtype=ParagraphStyleSubtype.paragraph)

    @classmethod
    def list_item(cls) -> Self:
        return cls(subtype=ParagraphStyleSubtype.list_item)
        # return cls(attributes={"class_name": "fake-li"})

    @property
    def macro(self) -> str:
        return f"paragraph_{self.subtype.name}"


class FunctionStyle(Style, frozen=True):
    type: FunctionStyleType | Literal[SpecialStyleType.color]
    value: str


class LinkStyle(Style, frozen=True):
    type: Literal[SpecialStyleType.link] = SpecialStyleType.link
    value: str | BookLink

    @pass_context
    def href(self, context: Context | dict[{"link_bases": BookLinkBases}]):
        match self.value:
            case str(href):
                return href
            case BookLink(as_tuple=key) as book_link:
                link_bases: BookLinkBases = context["link_bases"]
                if key not in link_bases:
                    raise ValueError(f"broken link: {book_link}")
                return link_bases[key] + book_link.fragment


# intentionally not inheriting from Style, because this is basically an implementation
# detail of the parser and should not be returned or exposed anywhere
class _CloseTag(HexdocModel, frozen=True):
    type: FunctionStyleType | Literal[
        SpecialStyleType.link,
        SpecialStyleType.base,
        SpecialStyleType.color,
    ]


_FORMAT_RE = re.compile(r"\$\(([^)]*)\)")


@dataclass(config=DEFAULT_CONFIG)
class FormatTree:
    style: Style
    children: list[FormatTree | str]  # this can't be Self, it breaks Pydantic

    @classmethod
    def format(
        cls,
        string: str,
        *,
        book_id: ResourceLocation,
        i18n: I18n,
        macros: dict[str, str],
        is_0_black: bool,
    ) -> Self:
        for macro, replace in macros.items():
            if macro in replace:
                raise RuntimeError(
                    f"Recursive macro: replacement `{replace}` is matched by key `{macro}`"
                )

        # resolve macros
        # this could use ahocorasick, but it works fine for now
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

            match Style.parse(match[1], book_id, i18n, is_0_black):
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
        if not info.context or isinstance(value, FormatTree):
            return handler(value)

        context = cast_or_raise(info.context, FormattingContext)
        if isinstance(value, str):
            value = context.i18n.localize(value)

        return cls.format(
            value.value,
            book_id=context.book_id,
            i18n=context.i18n,
            macros=context.macros,
            is_0_black=context.props.is_0_black,
        )


FormatTree._wrap_root
