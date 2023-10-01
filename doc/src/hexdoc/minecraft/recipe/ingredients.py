from pydantic import ValidationInfo, model_validator

from hexdoc.minecraft import RenderedItemStack
from hexdoc.minecraft.assets import TAG_TEXTURE
from hexdoc.minecraft.i18n import I18nContext
from hexdoc.utils import HexdocModel, NoValue, ResourceLocation, TypeTaggedUnion
from hexdoc.utils.deserialize import cast_or_raise


class ItemIngredient(TypeTaggedUnion, type=None):
    pass


ItemIngredientOrList = ItemIngredient | list[ItemIngredient]


class MinecraftItemIdIngredient(ItemIngredient, type=NoValue):
    item: RenderedItemStack


class MinecraftItemTagIngredient(ItemIngredient, type=NoValue):
    tag: ResourceLocation
    item: RenderedItemStack | None = None

    @model_validator(mode="after")
    def _add_item(self, info: ValidationInfo):
        if not info.context:
            return self
        context = cast_or_raise(info.context, I18nContext)

        self.item = RenderedItemStack(
            namespace=self.tag.namespace,
            path=self.tag.path,
            name=context.i18n.localize_tag(self.tag),
            texture=TAG_TEXTURE,
        )

        return self


class ItemResult(HexdocModel):
    item: RenderedItemStack
    count: int = 1
