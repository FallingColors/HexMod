import logging
from typing import Any, Self, cast

from pydantic import ValidationInfo, model_validator

from hexdoc.utils import AnyPropsContext, ResourceLocation, TypeTaggedUnion
from hexdoc.utils.deserialize import load_json_dict


class Recipe(TypeTaggedUnion[AnyPropsContext], group="hexdoc.Recipe", type=None):
    id: ResourceLocation

    group: str | None = None
    category: str | None = None

    @model_validator(mode="before")
    def _pre_root(
        cls,
        values: str | ResourceLocation | dict[str, Any] | Self,
        info: ValidationInfo,
    ):
        """Loads the recipe from json if the actual value is a resource location str."""
        context = cast(AnyPropsContext, info.context)
        if not context or isinstance(values, (dict, Recipe)):
            return values

        # if necessary, convert the id to a ResourceLocation
        match values:
            case str():
                id = ResourceLocation.from_str(values)
            case ResourceLocation():
                id = values

        # load the recipe
        path = context["props"].find_resource("data", "recipes", id)
        logging.getLogger(__name__).debug(f"Load {cls}\n  id:   {id}\n  path: {path}")
        return load_json_dict(path) | {"id": id}
