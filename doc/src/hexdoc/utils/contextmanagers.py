from contextlib import contextmanager
from contextvars import ContextVar
from typing import TypeVar

_T = TypeVar("_T")


@contextmanager
def set_contextvar(contextvar: ContextVar[_T], value: _T):
    token = contextvar.set(value)
    try:
        yield
    finally:
        contextvar.reset(token)
