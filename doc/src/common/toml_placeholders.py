import datetime
import re
from typing import Callable, TypeVar

from common.types import isinstance_or_raise

# TODO: there's (figuratively) literally no comments in this file

TOMLDict = dict[str, "TOMLValue"]

TOMLValue = (
    str
    | int
    | float
    | bool
    | datetime.datetime
    | datetime.date
    | datetime.time
    | list["TOMLValue"]
    | TOMLDict
)


def fill_placeholders(data: TOMLDict):
    _fill_placeholders(data, [data], set())


def _expand_placeholder(
    data: TOMLDict,
    stack: list[TOMLDict],
    expanded: set[tuple[int, str | int]],
    placeholder: str,
) -> str:
    tmp_stack: list[TOMLDict] = stack[:]

    key = "UNBOUND"
    keys = placeholder.split(".")
    for i, key in enumerate(keys):
        if n := key.count("^"):
            tmp_stack = tmp_stack[:-n]
            key = key.replace("^", "")
        if key and i < len(keys) - 1:
            # TODO: does this work?
            assert isinstance_or_raise(new := tmp_stack[-1][key], TOMLDict)
            tmp_stack.append(new)

    table = tmp_stack[-1]
    if (id(table), key) not in expanded:
        _handle_child(data, tmp_stack, expanded, key, table[key], table.__setitem__)

    assert isinstance_or_raise(value := table[key], str)
    return value


_T_key = TypeVar("_T_key", str, int)

_PLACEHOLDER_RE = re.compile(r"\{(.+?)\}")


def _handle_child(
    data: TOMLDict,
    stack: list[TOMLDict],
    expanded: set[tuple[int, str | int]],
    key: _T_key,
    value: TOMLValue,
    update: Callable[[_T_key, TOMLValue], None],
):
    # wait no that sounds wrong-
    match value:
        case str():
            # fill the string's placeholders
            for match in reversed(list(_PLACEHOLDER_RE.finditer(value))):
                try:
                    v = _expand_placeholder(
                        data,
                        stack,
                        expanded,
                        match[1],
                    )
                    value = value[: match.start()] + v + value[match.end() :]
                except Exception as e:
                    e.add_note(f"{match[0]} @ {value} @ {stack[-1]}")
                    raise
            expanded.add((id(stack[-1]), key))
            update(key, value)

        case {"_Raw": raw} if len(value) == 1:
            # interpolaten't
            expanded.add((id(stack[-1]), key))
            update(key, raw)

        case list():
            # handle each item in the list without adding the list to the stack
            for i, item in enumerate(value):
                _handle_child(data, stack, expanded, i, item, value.__setitem__)

        case dict():
            # recursion!
            _fill_placeholders(data, stack + [value], expanded)

        case _:
            pass


def _fill_placeholders(
    data: TOMLDict,
    stack: list[TOMLDict],
    expanded: set[tuple[int, str | int]],
):
    table = stack[-1]
    for key, child in table.items():
        _handle_child(data, stack, expanded, key, child, table.__setitem__)
