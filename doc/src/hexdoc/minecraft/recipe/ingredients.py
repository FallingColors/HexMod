from hexdoc.utils import NoValue, ResourceLocation, TypeTaggedUnion


class ItemIngredient(TypeTaggedUnion, type=None):
    pass


ItemIngredientOrList = ItemIngredient | list[ItemIngredient]


class MinecraftItemIdIngredient(ItemIngredient, type=NoValue):
    item: ResourceLocation


class MinecraftItemTagIngredient(ItemIngredient, type=NoValue):
    tag: ResourceLocation
