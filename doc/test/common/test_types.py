import pytest
from common.types import Color, ItemStack, ResourceLocation

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
    assert Color(s) == "0099ff"


resource_locations: list[tuple[str, ResourceLocation, str]] = [
    (
        "stone",
        ResourceLocation("minecraft", "stone"),
        "minecraft:",
    ),
    (
        "hexcasting:patchouli_book",
        ResourceLocation("hexcasting", "patchouli_book"),
        "",
    ),
]


@pytest.mark.parametrize("s,expected,str_prefix", resource_locations)
def test_resourcelocation(s: str, expected: ResourceLocation, str_prefix: str):
    actual = ResourceLocation.from_str(s)
    assert actual == expected
    assert str(actual) == str_prefix + s


item_stacks: list[tuple[str, ItemStack, str]] = [
    (
        "stone",
        ItemStack("minecraft", "stone", None, None),
        "minecraft:",
    ),
    (
        "hexcasting:patchouli_book",
        ItemStack("hexcasting", "patchouli_book", None, None),
        "",
    ),
    (
        "minecraft:stone#64",
        ItemStack("minecraft", "stone", 64, None),
        "",
    ),
    (
        "minecraft:diamond_pickaxe{display:{Lore:['A really cool pickaxe']}",
        ItemStack(
            "minecraft",
            "diamond_pickaxe",
            None,
            "{display:{Lore:['A really cool pickaxe']}",
        ),
        "",
    ),
    (
        "minecraft:diamond_pickaxe#64{display:{Lore:['A really cool pickaxe']}",
        ItemStack(
            "minecraft",
            "diamond_pickaxe",
            64,
            "{display:{Lore:['A really cool pickaxe']}",
        ),
        "",
    ),
]


@pytest.mark.parametrize("s,expected,str_prefix", item_stacks)
def test_itemstack(s: str, expected: ItemStack, str_prefix: str):
    actual = ItemStack.from_str(s)
    assert actual == expected
    assert str(actual) == str_prefix + s
