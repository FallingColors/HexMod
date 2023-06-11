from __future__ import annotations

import string
from typing import Self


class Color(str):
    """Newtype-style class representing a hexadecimal color.

    Inputs are coerced to lowercase `rrggbb`. Raises ValueError on invalid input.

    Valid formats, all of which would be converted to `0099ff`:
    - `#0099FF`
    - `#0099ff`
    - `#09F`
    - `#09f`
    - `0099FF`
    - `0099ff`
    - `09F`
    - `09f`
    """

    __slots__ = ()

    def __new__(cls, s: str) -> Self:
        color = s.removeprefix("#").lower()

        # 012 -> 001122
        if len(color) == 3:
            color = "".join(c + c for c in color)

        # length and character check
        if len(color) != 6 or any(c not in string.hexdigits for c in color):
            raise ValueError(f"invalid color code: {s}")

        return str.__new__(cls, color)
