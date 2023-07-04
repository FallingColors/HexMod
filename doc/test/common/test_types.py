import pytest

from common.types import Color

colors: list[str] = [
    "#0099FF",
    "#0099ff",
    "#09F",
    "#09f",
    "0099FF",
    "0099ff",
    "09F",
    "09f",
]


@pytest.mark.parametrize("s", colors)
def test_color(s: str):
    assert Color(s).value == "0099ff"
