from dataclasses import dataclass

from minecraft.resource import ResourceLocation


@dataclass
class ItemIngredientData:
    item: ResourceLocation | None = None
    tag: ResourceLocation | None = None


ItemIngredient = ItemIngredientData | list[ItemIngredientData]
