from enum import Enum
from functools import total_ordering
from typing import Any, Callable, Self

from hexdoc.__gradle_version__ import GRADLE_VERSION as HEX_VERSION


@total_ordering
class HexVersion(Enum):
    v0_11_x = "0.11."
    v0_10_x = "0.10."
    v0_9_x = "0.9."

    @classmethod
    def get(cls):
        for version in cls:
            if HEX_VERSION.startswith(version.value):
                return version
        raise ValueError(f"Unknown mod version: {HEX_VERSION}")

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
        raise ValueError(f"{typ} is not supported in {HEX_VERSION}")

    @property
    def _sort_by(self):
        return tuple(int(n) if n else 0 for n in self.value.split("."))

    def __lt__(self, other: Self):
        return self._sort_by < other._sort_by
