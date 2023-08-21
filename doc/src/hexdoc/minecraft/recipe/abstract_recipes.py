import logging
from typing import Any

from pydantic import ValidationInfo, model_validator

from hexdoc.utils import ResourceLocation, TypeTaggedUnion
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.resource_loader import LoaderContext


class Recipe(TypeTaggedUnion, group="hexdoc.Recipe", type=None):
    id: ResourceLocation

    group: str | None = None
    category: str | None = None

    @model_validator(mode="before")
    def _pre_root(cls, values: Any, info: ValidationInfo):
        """Loads the recipe from json if the actual value is a resource location str."""
        if not info.context:
            return values
        context = cast_or_raise(info.context, LoaderContext)

        # if necessary, convert the id to a ResourceLocation
        match values:
            case str():
                id = ResourceLocation.from_str(values)
            case ResourceLocation():
                id = values
            case _:
                return values

        # load the recipe
        _, data = context.loader.load_resource("data", "recipes", id)
        logging.getLogger(__name__).debug(f"Load {cls} from {id}")
        return data | {"id": id}
