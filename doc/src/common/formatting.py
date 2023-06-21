import re
from dataclasses import dataclass
from typing import NamedTuple, Self

from common.types import LocalizedStr

_COLORS: dict[str, str | None] = {
    "0": None,
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

_TYPES = {
    "k": "obf",
    "l": "bold",
    "m": "strikethrough",
    "n": "underline",
    "o": "italic",
}

_KEYS = {
    "use": "Right Click",
    "sneak": "Left Shift",
}


class Style(NamedTuple):
    type: str
    value: str | bool | dict[str, str] | None


# TODO: make Style a dataclass, subclass for each type
def parse_style(style_text: str) -> Style | str:
    if style_text in _TYPES:
        return Style(_TYPES[style_text], True)
    if style_text in _COLORS:
        return Style("color", _COLORS[style_text])
    if style_text.startswith("#") and len(style_text) in [4, 7]:
        return Style("color", style_text[1:])

    # try matching the entire string
    match style_text:
        # replacements
        case "br":
            return "\n"
        case "playername":
            return "[Playername]"
        # styles
        case "br2":
            return Style("para", {})
        case "li":
            return Style("para", {"clazz": "fake-li"})
        case "/l":
            return Style("link", None)
        case "/t":
            return Style("tooltip", None)
        case "/c":
            return Style("cmd_click", None)
        case "reset" | "":
            # TODO: this was "r" before, but patchouli's code has "reset"
            # the tests pass either way so I don't think we're using it
            return Style("base", None)
        case _:
            pass

    # command prefixes
    command, rest = style_text[:2], style_text[2:]
    match command:
        # replacement
        case "k:":
            return _KEYS[rest]
        # styles
        case "l:":
            return Style("link", rest)
        case "t:":
            return Style("tooltip", rest)
        case "c:":
            return Style("cmd_click", rest)
        case _:
            # TODO more style parse
            raise ValueError("Unknown style: " + style_text)


_FORMAT_RE = re.compile(r"\$\(([^)]*)\)")


@dataclass
class FormatTree:
    style: Style
    children: list[Self | str]

    @classmethod
    def empty(cls) -> Self:
        return cls(Style("base", None), [])

    @classmethod
    def format(cls, macros: dict[str, str], string: LocalizedStr) -> Self:
        # resolve macros
        # TODO: use ahocorasick? this feels inefficient
        old_string = None
        while old_string != string:
            old_string = string
            for macro, replace in macros.items():
                string = LocalizedStr(string.replace(macro, replace))

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

            match parse_style(match[1]):
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
            FormatTree(Style("base", True), []),
            FormatTree(Style("para", {}), [first_node]),
        ]
        for style, text in zip(styles, text_nodes):
            tmp_stylestack: list[Style] = []
            if style.type == "base":
                while style_stack[-1].style.type != "para":
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
            if style.value is None:
                if text:
                    style_stack[-1].children.append(text)
            else:
                style_stack.append(FormatTree(style, [text] if text else []))
        while len(style_stack) >= 2:
            last_node = style_stack.pop()
            style_stack[-1].children.append(last_node)

        return style_stack[0]
