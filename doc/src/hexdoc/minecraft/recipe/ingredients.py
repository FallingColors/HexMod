from hexdoc.minecraft.assets.textures import ItemWithTexture, TagWithTexture
from hexdoc.utils import HexdocModel, NoValue, TypeTaggedUnion


class ItemIngredient(TypeTaggedUnion, type=None):
    pass


ItemIngredientOrList = ItemIngredient | list[ItemIngredient]


class MinecraftItemIdIngredient(ItemIngredient, type=NoValue):
    item: ItemWithTexture


class MinecraftItemTagIngredient(ItemIngredient, type=NoValue):
    tag: TagWithTexture

    @property
    def item(self):
        return self.tag


class ItemResult(HexdocModel):
    item: ItemWithTexture
    count: int = 1
