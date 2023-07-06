# pyright: reportPrivateUsage=false

from __future__ import annotations

import contextlib
import re
from abc import ABC, abstractmethod
from dataclasses import InitVar, dataclass as py_dataclass
from html import escape
from typing import IO, Any, Callable, ContextManager, Self, cast

from pydantic import ValidationInfo, model_validator
from pydantic.dataclasses import dataclass
from pydantic.functional_validators import ModelWrapValidatorHandler

from common.model import DEFAULT_CONFIG
from common.types import NoClobberDict
from minecraft.i18n import I18nContext, LocalizedStr

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

_COLORS = {
    # "0": None, # TODO: find an actual value for this
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

# TODO: type
def tag_args(kwargs: dict[str, Any]):
    return "".join(
        f" {'class' if key == 'clazz' else key.replace('_', '-')}={repr(escape(str(value)))}"
        for key, value in kwargs.items()
    )


@py_dataclass
class PairTag:
    stream: IO[str]
    name: str
    args: InitVar[dict[str, Any]]

    def __post_init__(self, args: dict[str, Any]):
        self.args_str = tag_args(args)

    def __enter__(self):
        # TODO: self.stream.write??????????
        print(f"<{self.name}{self.args_str}>", file=self.stream, end="")

    def __exit__(self, _1: Any, _2: Any, _3: Any):
        print(f"</{self.name}>", file=self.stream, end="")


class Empty:
    def __enter__(self):
        pass

    def __exit__(self, _1: Any, _2: Any, _3: Any):
        pass


class Stream:
    __slots__ = ["stream"]

    def __init__(self, stream: IO[str]):
        self.stream = stream

    def tag(self, name: str, **kwargs: Any):
        keywords = tag_args(kwargs)
        print(f"<{name}{keywords} />", file=self.stream, end="")
        return self

    def pair_tag(self, name: str, **kwargs: Any):
        return PairTag(self.stream, name, kwargs)

    def pair_tag_if(self, cond: Any, name: str, **kwargs: Any):
        return self.pair_tag(name, **kwargs) if cond else Empty()

    def empty_pair_tag(self, name: str, **kwargs: Any):
        with self.pair_tag(name, **kwargs):
            pass

    def text(self, txt: str | LocalizedStr):
        print(escape(str(txt)), file=self.stream, end="")
        return self


_COMMAND_LOOKUPS: list[CommandLookup] = []
_COMMANDS: dict[str, StyleCommand] = NoClobberDict()
_FUNCTIONS: dict[str, StyleFunction] = NoClobberDict()


def command_lookup(fn: CommandLookup) -> CommandLookup:
    _COMMAND_LOOKUPS.append(fn)
    return fn


# TODO: refactor literally all of everything, this is still pretty disgusting
@dataclass(config=DEFAULT_CONFIG)
class Style(ABC):
    @classmethod
    def parse(cls, style_text: str) -> Style | str:
        # command lookups (includes commands and functions)
        for lookup in _COMMAND_LOOKUPS:
            if (style := lookup(style_text)) is not None:
                return style

        # oopsies
        raise ValueError(f"Unhandled style: {style_text}")

    @abstractmethod
    def tag(self, out: Stream) -> ContextManager[None]:
        ...

    def can_close(self, other: Style) -> bool:
        return isinstance(self, type(other)) or isinstance(other, type(self))


@command_lookup
def replacement_processor(name: str):
    match name:
        case "br":
            return "\n"
        case "playername":
            return "[Playername]"
        case _:
            return None


@command_lookup
def command_processor(name: str):
    if style_type := _COMMANDS.get(name):
        return style_type()


@command_lookup
def function_processor(style_text: str):
    if ":" in style_text:
        name, param = style_text.split(":", 1)
        if style_type := _FUNCTIONS.get(name):
            return style_type(param)
        raise ValueError(f"Unhandled function: {style_text}")


@dataclass(config=DEFAULT_CONFIG)
class BaseStyleCommand(Style):
    def __init_subclass__(cls, names: list[str]) -> None:
        for name in names:
            _COMMANDS[name] = cls


@dataclass(config=DEFAULT_CONFIG)
class EndFunctionStyle(BaseStyleCommand, names=[]):
    function_type: type[BaseStyleFunction]

    def tag(self, out: Stream):
        return contextlib.nullcontext()

    def can_close(self, other: Style) -> bool:
        return super().can_close(other) or isinstance(other, self.function_type)


@dataclass(config=DEFAULT_CONFIG)
class BaseStyleFunction(Style):
    value: str

    def __init_subclass__(cls, names: list[str]) -> None:
        for name in names:
            _FUNCTIONS[name] = cls
            _COMMANDS[f"/{name}"] = lambda: EndFunctionStyle(cls)


CommandLookup = Callable[[str], Style | str | None]

StyleCommand = Callable[[], Style | str]

StyleFunction = Callable[[str], BaseStyleFunction | str]


def style_function(name: str):
    def wrap(fn: StyleFunction) -> StyleFunction:
        _FUNCTIONS[name] = fn
        return fn

    return wrap


class ClearStyle(BaseStyleCommand, names=[""]):
    def tag(self, out: Stream):
        return contextlib.nullcontext()


class ParagraphStyle(BaseStyleCommand, names=["br2"]):
    def tag(self, out: Stream):
        return out.pair_tag("p")


class ListItemStyle(ParagraphStyle, names=["li"]):
    def tag(self, out: Stream):
        return out.pair_tag("p", clazz="fake-li")


class ObfuscatedStyle(BaseStyleCommand, names=["k"]):
    def tag(self, out: Stream):
        return out.pair_tag("span", clazz="obfuscated")


class BoldStyle(BaseStyleCommand, names=["l"]):
    def tag(self, out: Stream):
        return out.pair_tag("strong")


class StrikethroughStyle(BaseStyleCommand, names=["m"]):
    def tag(self, out: Stream):
        return out.pair_tag("s")


class UnderlineStyle(BaseStyleCommand, names=["n"]):
    def tag(self, out: Stream):
        return out.pair_tag("span", style="text-decoration: underline")


class ItalicStyle(BaseStyleCommand, names=["o"]):
    def tag(self, out: Stream):
        return out.pair_tag("i")


@style_function("k")
def get_keybind_key(param: str):
    if (key := _KEYS.get(param)) is not None:
        return key
    raise ValueError(f"Unhandled key: {param}")


# TODO: this should use Color but i'm pretty sure that will fail the snapshots
class ColorStyle(BaseStyleFunction, names=[]):
    @command_lookup
    @staticmethod
    def processor(param: str):
        if param in _COLORS:
            return ColorStyle(_COLORS[param])
        if param.startswith("#") and len(param) in [4, 7]:
            return ColorStyle(param[1:])

    def tag(self, out: Stream):
        return out.pair_tag("span", style=f"color: #{self.value}")


class LinkStyle(BaseStyleFunction, names=["l"]):
    def tag(self, out: Stream):
        href = self.value
        if not href.startswith(("http:", "https:")):
            href = "#" + href.replace("#", "@")
        return out.pair_tag("a", href=href)


class TooltipStyle(BaseStyleFunction, names=["t"]):
    def tag(self, out: Stream):
        return out.pair_tag("span", clazz="has-tooltip", title=self.value)


class CmdClickStyle(BaseStyleFunction, names=["c"]):
    def tag(self, out: Stream):
        return out.pair_tag(
            "span",
            clazz="has-cmd_click",
            title=f"When clicked, would execute: {self.value}",
        )


# class GangnamStyle: pass


_FORMAT_RE = re.compile(r"\$\(([^)]*)\)")


class FormatContext(I18nContext):
    macros: dict[str, str]


@dataclass(config=DEFAULT_CONFIG)
class FormatTree:
    style: Style
    children: list[FormatTree | str]  # this can't be Self, it breaks Pydantic

    @classmethod
    def empty(cls) -> Self:
        return cls(ClearStyle(), [])

    @classmethod
    def format(cls, string: str, macros: dict[str, str]) -> Self:
        # resolve macros
        # TODO: use ahocorasick? this feels inefficient
        old_string = None
        while old_string != string:
            old_string = string
            for macro, replace in macros.items():
                string = string.replace(macro, replace)

        # lex out parsed styles
        text_nodes: list[str] = []
        styles: list[Style] = []
        text_since_prev_style: list[str] = []
        last_end = 0

        for match in re.finditer(_FORMAT_RE, string):
            # get the text between the previous match and here
            leading_text = string[last_end : match.start()]
            text_since_prev_style.append(leading_text)
            last_end = match.end()

            match Style.parse(match[1]):
                case str(replacement):
                    # str means "use this instead of the original value"
                    text_since_prev_style.append(replacement)
                case Style() as style:
                    # add this style and collect the text since the previous one
                    styles.append(style)
                    text_nodes.append("".join(text_since_prev_style))
                    text_since_prev_style.clear()

        text_nodes.append("".join(text_since_prev_style) + string[last_end:])
        first_node = text_nodes.pop(0)

        # parse
        style_stack = [
            FormatTree(ClearStyle(), []),
            FormatTree(ParagraphStyle(), [first_node]),
        ]
        for style, text in zip(styles, text_nodes):
            tmp_stylestack: list[Style] = []
            if isinstance(style, ClearStyle):
                while not isinstance(style_stack[-1].style, ParagraphStyle):
                    last_node = style_stack.pop()
                    style_stack[-1].children.append(last_node)
            elif any(style.can_close(tree.style) for tree in style_stack):
                while len(style_stack) >= 2:
                    last_node = style_stack.pop()
                    style_stack[-1].children.append(last_node)
                    if style.can_close(last_node.style):
                        break
                    tmp_stylestack.append(last_node.style)

            for sty in tmp_stylestack:
                style_stack.append(FormatTree(sty, []))

            if isinstance(style, (EndFunctionStyle, ClearStyle)):
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
        return cls.format(value.value, context["macros"])
