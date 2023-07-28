import json
from pathlib import Path
from typing import Any, TypeGuard, TypeVar, get_origin

_T = TypeVar("_T")

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
    data: JSONValue = json.loads(path.read_text("utf-8"))
    assert isinstance_or_raise(data, dict)
    return data
