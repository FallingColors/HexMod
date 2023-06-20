from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Self

import patchouli
from common.deserialize import TypeFn, load_json_data
from common.tagged_union import InternallyTaggedUnion
from minecraft.resource import ResourceLocation


@dataclass(kw_only=True)
class BaseRecipe(InternallyTaggedUnion, tag="type", value=None):
    id: ResourceLocation

    type: ResourceLocation = field(init=False)
    group: str | None = None

    def __init_subclass__(cls, type: str) -> None:
        super().__init_subclass__(__class__._tag_name, type)
        cls.type = ResourceLocation.from_str(type)

    @classmethod
    def make_type_hook(cls, book: patchouli.Book) -> TypeFn:
        """Creates a type hook which, given a stringified ResourceLocation, loads and
        returns the recipe json at that location."""

        def type_hook(raw_id: str | Any) -> Self | dict[str, Any]:
            if isinstance(raw_id, cls):
                return raw_id
            id = ResourceLocation.from_str(raw_id)

            # FIXME: hack
            # the point of this is to ensure the recipe exists on all platforms
            # because we've had issues with that in the past, eg. in Hexal
            data: dict[str, Any] = {}
            for recipe_dir in book.props.recipe_dirs:
                # TODO: should this use id.namespace somewhere?
                data = load_json_data(cls, recipe_dir / f"{id.path}.json")

            data["id"] = id
            return data

        return type_hook
