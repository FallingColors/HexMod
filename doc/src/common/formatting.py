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


def parse_style(sty: str) -> tuple[str, Style | None]:
    # TODO: match, maybe
    if sty == "br":
        return "\n", None
    if sty == "br2":
        return "", Style("para", {})
    if sty == "li":
        return "", Style("para", {"clazz": "fake-li"})
    if sty[:2] == "k:":
        return _KEYS[sty[2:]], None
    if sty[:2] == "l:":
        return "", Style("link", sty[2:])
    if sty == "/l":
        return "", Style("link", None)
    if sty == "playername":
        return "[Playername]", None
    if sty[:2] == "t:":
        return "", Style("tooltip", sty[2:])
    if sty == "/t":
        return "", Style("tooltip", None)
    if sty[:2] == "c:":
        return "", Style("cmd_click", sty[2:])
    if sty == "/c":
        return "", Style("cmd_click", None)
    if sty == "r" or not sty:
        return "", Style("base", None)
    if sty in _TYPES:
        return "", Style(_TYPES[sty], True)
    if sty in _COLORS:
        return "", Style("color", _COLORS[sty])
    if sty.startswith("#") and len(sty) in [4, 7]:
        return "", Style("color", sty[1:])
    # TODO more style parse
    raise ValueError("Unknown style: " + sty)


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
        # FIXME: ew.

        # resolve macros
        # TODO: use ahocorasick?
        old_string = None
        while old_string != string:
            old_string = string
            for macro, replace in macros.items():
                string = LocalizedStr(string.replace(macro, replace))

        # lex out parsed styles
        text_nodes: list[str] = []
        styles: list[Style] = []
        last_end = 0
        extra_text = ""
        for mobj in re.finditer(_FORMAT_RE, string):
            bonus_text, sty = parse_style(mobj.group(1))
            text = string[last_end : mobj.start()] + bonus_text
            if sty:
                styles.append(sty)
                text_nodes.append(extra_text + text)
                extra_text = ""
            else:
                extra_text += text
            last_end = mobj.end()
        text_nodes.append(extra_text + string[last_end:])
        first_node, *text_nodes = text_nodes

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
