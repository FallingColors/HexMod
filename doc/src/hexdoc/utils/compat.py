from enum import Enum
from functools import total_ordering
from typing import Any, Callable, Self

from hexdoc.__gradle_version__ import GRADLE_VERSION


@total_ordering
class HexVersion(Enum):
    v0_11 = "0.11."
    v0_10 = "0.10."
    v0_9 = "0.9."

    @classmethod
    def get(cls):
        for version in cls:
            if GRADLE_VERSION.startswith(version.value):
                return version
        raise ValueError(f"Unknown mod version: {GRADLE_VERSION}")

    @classmethod
    def check(cls, version: Self | Callable[[Self], bool] | bool, typ: type[Any] | str):
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

        if not isinstance(typ, str):
            typ = typ.__name__
        raise ValueError(f"{typ} is not supported in {GRADLE_VERSION}")

    @property
    def _sort_by(self):
        return tuple(int(n) if n else 0 for n in self.value.split("."))

    def __lt__(self, other: Self):
        return self._sort_by < other._sort_by
