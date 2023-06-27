from dataclasses import dataclass

from common.tagged_union import NoValue
from minecraft.resource import ResourceLocation
from patchouli.state import AnyState, BookState, StatefulTypeTaggedUnion


class ItemIngredient(StatefulTypeTaggedUnion[AnyState], type=None):
    pass


ItemIngredientOrList = ItemIngredient[AnyState] | list[ItemIngredient[AnyState]]


@dataclass
class MinecraftItemIdIngredient(ItemIngredient[BookState], type=NoValue):
    item: ResourceLocation


@dataclass
class MinecraftItemTagIngredient(ItemIngredient[BookState], type=NoValue):
    tag: ResourceLocation
