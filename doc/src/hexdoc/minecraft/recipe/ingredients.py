from typing import Annotated, Any, Iterator

from pydantic import AfterValidator, BeforeValidator, ValidationError, ValidationInfo

from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft.assets.textures import (
    ItemWithTexture,
    TagWithTexture,
    TextureContext,
)
from hexdoc.minecraft.tags import Tag
from hexdoc.model import HexdocModel
from hexdoc.model.tagged_union import NoValue, TypeTaggedUnion
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.iterators import listify


class ItemIngredient(TypeTaggedUnion, type=None):
    pass


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


def to_list(value: Any):
    match value:
        case [*contents]:
            return contents
        case _:
            return [value]


@listify
def unwrap_tags(
    ingredients: list[ItemIngredient],
    info: ValidationInfo,
) -> Iterator[ItemIngredient]:
    context = cast_or_raise(info.context, TextureContext)

    for ingredient in ingredients:
        yield ingredient

        if isinstance(ingredient, MinecraftItemTagIngredient):
            yield from unwrap_tag(ingredient.tag.id, context)


def unwrap_tag(
    tag_id: ResourceLocation,
    context: TextureContext,
) -> Iterator[ItemIngredient]:
    try:
        tag = Tag.load("items", tag_id, context)
    except FileNotFoundError:
        return

    for id in tag.values:
        try:
            yield MinecraftItemIdIngredient.model_validate(
                {"item": id},
                context=context,
            )
        except ValidationError:
            yield from unwrap_tag(id, context)


ItemIngredientList = Annotated[
    list[ItemIngredient],
    BeforeValidator(to_list),
    AfterValidator(unwrap_tags),
]
