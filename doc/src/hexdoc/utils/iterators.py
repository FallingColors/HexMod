import functools
from typing import Callable, Iterator, ParamSpec, TypeVar

_T = TypeVar("_T")
_P = ParamSpec("_P")


def must_yield_something(f: Callable[_P, Iterator[_T]]) -> Callable[_P, Iterator[_T]]:
    """Raises StopIteration if the wrapped iterator doesn't yield anything."""

    @functools.wraps(f)
    def wrapper(*args: _P.args, **kwargs: _P.kwargs) -> Iterator[_T]:
        iterator = f(*args, **kwargs)
        yield next(iterator)
        yield from iterator

    return wrapper


def listify(f: Callable[_P, Iterator[_T]]) -> Callable[_P, list[_T]]:
    @functools.wraps(f)
    def wrapper(*args: _P.args, **kwargs: _P.kwargs) -> list[_T]:
        return list(f(*args, **kwargs))

    return wrapper
