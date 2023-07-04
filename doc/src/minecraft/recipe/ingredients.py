from common.tagged_union import NoValue, TypeTaggedUnion
from minecraft.resource import ResourceLocation
from patchouli.context import AnyBookContext, BookContext


class ItemIngredient(
    TypeTaggedUnion[AnyBookContext],
    group="hexdoc.ItemIngredient",
    type=None,
):
    pass


ItemIngredientOrList = (
    ItemIngredient[AnyBookContext] | list[ItemIngredient[AnyBookContext]]
)


class MinecraftItemIdIngredient(ItemIngredient[BookContext], type=NoValue):
    item: ResourceLocation


class MinecraftItemTagIngredient(ItemIngredient[BookContext], type=NoValue):
    tag: ResourceLocation
