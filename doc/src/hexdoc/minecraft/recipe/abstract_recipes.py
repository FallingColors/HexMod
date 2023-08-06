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
        if not info.context or isinstance(values, (dict, Recipe)):
            return values

        # if necessary, convert the id to a ResourceLocation
        match values:
            case str():
                id = ResourceLocation.from_str(values)
            case ResourceLocation():
                id = values

        # load the recipe
        context = cast(AnyPropsContext, info.context)

        # TODO: this is ugly and not super great for eg. hexbound
        forge_path = context["props"].forge.recipes / f"{id.path}.json"
        fabric_path = context["props"].fabric.recipes / f"{id.path}.json"

        # this is to ensure the recipe at least exists on all platforms
        # because we've had issues with that before (eg. Hexal's Mote Nexus)
        if not forge_path.exists():
            raise ValueError(f"Recipe {id} missing from path {forge_path}")

        logging.getLogger(__name__).debug(
            f"Load {cls}\n  id:   {id}\n  path: {fabric_path}"
        )
        return load_json_dict(fabric_path) | {"id": id}
