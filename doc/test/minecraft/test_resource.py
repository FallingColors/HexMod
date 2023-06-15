import pytest
from minecraft.resource import ItemStack, ResourceLocation

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
        ItemStack.from_parts("minecraft", "stone", None, None),
        "minecraft:",
    ),
    (
        "hexcasting:patchouli_book",
        ItemStack.from_parts("hexcasting", "patchouli_book", None, None),
        "",
    ),
    (
        "minecraft:stone#64",
        ItemStack.from_parts("minecraft", "stone", 64, None),
        "",
    ),
    (
        "minecraft:diamond_pickaxe{display:{Lore:['A really cool pickaxe']}",
        ItemStack.from_parts(
            "minecraft",
            "diamond_pickaxe",
            None,
            "{display:{Lore:['A really cool pickaxe']}",
        ),
        "",
    ),
    (
        "minecraft:diamond_pickaxe#64{display:{Lore:['A really cool pickaxe']}",
        ItemStack.from_parts(
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
