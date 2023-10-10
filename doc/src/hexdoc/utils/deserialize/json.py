import re
from typing import TypeVar

import pyjson5

from .assertions import isinstance_or_raise

_T_co = TypeVar("_T_co", covariant=True)

JSONDict = dict[str, "JSONValue"]

JSONValue = JSONDict | list["JSONValue"] | str | int | float | bool | None


def decode_json_dict(data: str | bytes) -> JSONDict:
    match data:
        case str():
            decoded = pyjson5.decode(data)
        case _:
            decoded = pyjson5.decode_utf8(data)
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


def _update_disallow_duplicates(base: dict[str, _T_co], new: dict[str, _T_co]):
    for key, value in new.items():
        if key in base:
            raise ValueError(f"Duplicate key {key}\nold=`{base[key]}`\nnew=`{value}`")
        base[key] = value
