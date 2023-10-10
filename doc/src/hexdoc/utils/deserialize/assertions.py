import logging
from typing import Any, TypeGuard, TypeVar, get_origin

_T = TypeVar("_T")

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
