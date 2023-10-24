# pyright: reportPrivateUsage=false

from enum import Enum
from typing import Final


class NoValueType(Enum):
    """Type of NoValue, a singleton representing the value of a nonexistent dict key."""

    _token = 0

    def __str__(self):
        return "NoValue"


NoValue: Final = NoValueType._token
"""A singleton (like None) representing the value of a nonexistent dict key."""


class InheritType(Enum):
    """Type of Inherit, a singleton representing a value that should be inherited."""

    _token = 0

    def __str__(self):
        return "Inherit"


Inherit: Final = InheritType._token
"""A singleton (like None) representing a value that should be inherited."""
