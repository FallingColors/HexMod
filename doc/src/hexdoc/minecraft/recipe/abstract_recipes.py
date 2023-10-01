import logging
from typing import Any, Self

from pydantic import ValidationInfo, model_validator
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.utils import ResourceLocation, TypeTaggedUnion
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.resource_loader import LoaderContext

from .ingredients import ItemResult


class Recipe(TypeTaggedUnion, type=None):
    id: ResourceLocation

    group: str | None = None
    category: str | None = None

    # use wrap validator so we load the file *before* resolving the tagged union
    @model_validator(mode="wrap")
    @classmethod
    def _pre_root(
        cls,
        value: Any,
        handler: ModelWrapValidatorHandler[Self],
        info: ValidationInfo,
    ) -> Self:
        """Loads the recipe from json if the actual value is a resource location str."""
        if not info.context:
            return handler(value)
        context = cast_or_raise(info.context, LoaderContext)

        # if necessary, convert the id to a ResourceLocation
        match value:
            case str():
                id = ResourceLocation.from_str(value)
            case ResourceLocation():
                id = value
            case _:
                return handler(value)

        # load the recipe
        _, data = context.loader.load_resource("data", "recipes", id)
        logging.getLogger(__name__).debug(f"Load {cls} from {id}")
        return handler(data | {"id": id})


class CraftingRecipe(Recipe, type=None):
    result: ItemResult
