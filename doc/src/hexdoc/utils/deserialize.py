import re
from pathlib import Path
from typing import Any, TypeGuard, TypeVar, get_origin

import pyjson5

_T = TypeVar("_T")
_T_cov = TypeVar("_T_cov", covariant=True)

_DEFAULT_MESSAGE = "Expected any of {expected}, got {actual}: {value}"


# there may well be a better way to do this but i don't know what it is
def isinstance_or_raise(
    val: Any,
    class_or_tuple: type[_T] | tuple[type[_T], ...],
    message: str = _DEFAULT_MESSAGE,
) -> TypeGuard[_T]:
    """Usage: `assert isinstance_or_raise(val, str)`

    message placeholders: `{expected}`, `{actual}`, `{value}`
    """

    # convert generic types into the origin type
    if not isinstance(class_or_tuple, tuple):
        class_or_tuple = (class_or_tuple,)
    ungenericed_classes = tuple(get_origin(t) or t for t in class_or_tuple)

    if not isinstance(val, ungenericed_classes):
        # just in case the caller messed up the message formatting
        subs = {"expected": class_or_tuple, "actual": type(val), "value": val}
        try:
            raise TypeError(message.format(**subs))
        except KeyError:
            raise TypeError(_DEFAULT_MESSAGE.format(**subs))
    return True


JSONDict = dict[str, "JSONValue"]

JSONValue = JSONDict | list["JSONValue"] | str | int | float | bool | None


def load_json_dict(path: Path) -> JSONDict:
    data = pyjson5.decode(path.read_text("utf-8"))
    assert isinstance_or_raise(data, dict)
    return data


# implement pkpcpbp's flattening in python
# https://github.com/gamma-delta/PKPCPBP/blob/786194a590f/src/main/java/at/petrak/pkpcpbp/filters/JsonUtil.java
def load_and_flatten_json_dict(path: Path) -> dict[str, str]:
    # load file, replace `\<LF>       foobar` with `\<LF>foobar`
    json_str = re.sub(r"\\\n\s*", "\\\n", path.read_text("utf-8"))

    # decode json5 and flatten
    data = pyjson5.decode(json_str)
    assert isinstance_or_raise(data, JSONDict)

    return _flatten_inner(data, "")


def _flatten_inner(obj: JSONDict, prefix: str) -> dict[str, str]:
    out: dict[str, str] = {}

    for key_stub, value in obj.items():
        if not prefix:
            key = key_stub
        elif not key_stub:
            key = prefix
        elif prefix[-1] in ":_-/":
            key = prefix + key_stub
        else:
            key = f"{prefix}.{key_stub}"

        match value:
            case dict():
                _update_disallow_duplicates(out, _flatten_inner(value, key))
            case str():
                _update_disallow_duplicates(out, {key: value})
            case _:
                raise TypeError(value)

    return out


def _update_disallow_duplicates(base: dict[str, _T_cov], new: dict[str, _T_cov]):
    for key, value in new.items():
        if key in base:
            raise ValueError(f"Duplicate key {key}\nold=`{base[key]}`\nnew=`{value}`")
        base[key] = value
