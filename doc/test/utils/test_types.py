import pytest
from pydantic import TypeAdapter, ValidationError

from hexdoc.utils.types import Color, PydanticOrderedSet

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


def test_ordered_set_round_trip():
    data = [3, 1, 3, 2, 1]
    ta = TypeAdapter(PydanticOrderedSet[int])

    ordered_set = ta.validate_python(data)

    assert ordered_set.items == [3, 1, 2]


def test_ordered_set_validation_error():
    data = [1, "a"]
    ta = TypeAdapter(PydanticOrderedSet[int])

    with pytest.raises(ValidationError):
        ta.validate_python(data)
