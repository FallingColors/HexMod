from hexdoc.utils import ModResourceLoader, ResourceLocation, TypeTaggedUnion
from hexdoc.utils.resource_model import InlineIDModel

from .ingredients import ItemResult


class Recipe(TypeTaggedUnion, InlineIDModel, type=None):
    group: str | None = None
    category: str | None = None

    @classmethod
    def load_resource(cls, id: ResourceLocation, loader: ModResourceLoader):
        return loader.load_resource("data", "recipes", id)


class CraftingRecipe(Recipe, type=None):
    result: ItemResult
