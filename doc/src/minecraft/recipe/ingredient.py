from dataclasses import dataclass
from typing import Literal

from minecraft.resource import ResourceLocation


@dataclass
class ItemIngredientData:
    item: ResourceLocation | None = None
    tag: ResourceLocation | None = None


# TODO: should be in hex but idk how
# TODO: tagged union~!
@dataclass
class ModConditionalIngredient:
    type: Literal["hexcasting:mod_conditional"]
    default: ItemIngredientData
    if_loaded: ItemIngredientData
    modid: str


ItemIngredient = (
    ItemIngredientData | ModConditionalIngredient | list[ItemIngredientData]
)
