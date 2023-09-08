import logging
import re
from typing import Any, TypeGuard, TypeVar, get_origin

import pyjson5

_T = TypeVar("_T")
_T_cov = TypeVar("_T_cov", covariant=True)

_DEFAULT_MESSAGE_SHORT = "Expected any of {expected}, got {actual}"
_DEFAULT_MESSAGE_LONG = "Expected any of {expected}, got {actual}: {value}"


def isinstance_or_raise(
    val: Any,
    class_or_tuple: type[_T] | tuple[type[_T], ...],
    message: str | None = None,
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
        subs = {
            "expected": list(class_or_tuple),
            "actual": type(val),
            "value": val,
        }

        if logging.getLogger(__name__).getEffectiveLevel() >= logging.WARNING:
            default_message = _DEFAULT_MESSAGE_SHORT
        else:
            default_message = _DEFAULT_MESSAGE_LONG

        if message is None:
            raise TypeError(default_message.format(**subs))

        try:
            raise TypeError(message.format(**subs))
        except KeyError:
            raise TypeError(default_message.format(**subs))

    return True


def cast_or_raise(
    val: Any,
    class_or_tuple: type[_T] | tuple[type[_T], ...],
    message: str | None = None,
) -> _T:
    assert isinstance_or_raise(val, class_or_tuple, message)
    return val


JSONDict = dict[str, "JSONValue"]

JSONValue = JSONDict | list["JSONValue"] | str | int | float | bool | None


def decode_json_dict(data: str) -> JSONDict:
    decoded = pyjson5.decode(data)
    assert isinstance_or_raise(decoded, dict)
    return decoded


# implement pkpcpbp's flattening in python
# https://github.com/gamma-delta/PKPCPBP/blob/786194a590f/src/main/java/at/petrak/pkpcpbp/filters/JsonUtil.java
def decode_and_flatten_json_dict(data: str) -> dict[str, str]:
    # replace `\<LF>       foobar` with `\<LF>foobar`
    data = re.sub(r"\\\n\s*", "\\\n", data)

    # decode and flatten
    decoded = decode_json_dict(data)
    return _flatten_inner(decoded, "")


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
