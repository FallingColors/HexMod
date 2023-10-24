from __future__ import annotations

from typing import Any, Callable, Generic, TypeVar

_T_cv = TypeVar("_T_cv", contravariant=True)
_R_co = TypeVar("_R_co", covariant=True)


# https://discuss.python.org/t/add-a-supported-read-only-classproperty-decorator-in-the-stdlib/18090
class ClassPropertyDescriptor(Generic[_T_cv, _R_co]):
    """Equivalent of `classmethod(property(...))`.

    Use `@classproperty`. Do not instantiate this class directly.
    """

    def __init__(self, func: classmethod[_T_cv, Any, _R_co]) -> None:
        self.func = func

    def __get__(self, _: Any, cls: type[_T_cv]) -> _R_co:
        return self.func.__func__(cls)


def classproperty(
    func: Callable[[type[_T_cv]], _R_co]
) -> ClassPropertyDescriptor[_T_cv, _R_co]:
    if isinstance(func, classmethod):
        return ClassPropertyDescriptor(func)
    return ClassPropertyDescriptor(classmethod(func))
