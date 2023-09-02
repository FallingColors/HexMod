from enum import Enum
from functools import total_ordering
from typing import Any, Callable, Self

from hexdoc.__version__ import GRADLE_VERSION


@total_ordering
class HexVersion(Enum):
    v0_11 = "0.11."
    v0_10 = "0.10."

    @classmethod
    def get(cls):
        for version in cls:
            if GRADLE_VERSION.startswith(version.value):
                return version
        raise ValueError(f"Unknown mod version: {GRADLE_VERSION}")

    @classmethod
    def check(cls, version: Self | Callable[[Self], bool] | bool, typ: type[Any]):
        match version:
            case HexVersion():
                if cls.get() is version:
                    return
            case bool():
                if version:
                    return
            case _:
                if version(cls.get()):
                    return

        raise ValueError(f"{typ.__name__} is not supported in {GRADLE_VERSION}")

    def __lt__(self, other: Self):
        return self.value < other.value
