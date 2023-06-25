from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Self, TypeVar

from common.deserialize import TypeHook, load_json_data
from common.state import BookState, StatefulInternallyTaggedUnion
from minecraft.resource import ResourceLocation

_T_State = TypeVar("_T_State", bound=BookState)


@dataclass(kw_only=True)
class Recipe(StatefulInternallyTaggedUnion[_T_State], tag="type", value=None):
    id: ResourceLocation

    type: ResourceLocation = field(init=False)
    group: str | None = None

    def __init_subclass__(cls, type: str) -> None:
        super().__init_subclass__(__class__._tag_key, type)
        cls.type = ResourceLocation.from_str(type)

    @classmethod
    def make_type_hook(cls, state: BookState) -> TypeHook[Self]:
        """Creates a type hook which, given a stringified ResourceLocation, loads and
        returns the recipe json at that location."""

        def type_hook(raw_id: str | Any) -> Self | dict[str, Any]:
            # FIXME: this should use isinstance_or_raise but I'm probably redoing it
            if isinstance(raw_id, cls):
                return raw_id

            # FIXME: hack
            # the point of this is to ensure the recipe exists on all platforms
            # because we've had issues with that in the past, eg. in Hexal
            id = ResourceLocation.from_str(raw_id)
            data: dict[str, Any] = {}
            for recipe_dir in state.props.recipe_dirs:
                # TODO: should this use id.namespace somewhere?
                data = load_json_data(cls, recipe_dir / f"{id.path}.json")

            return data | {"id": id, "state": state}

        return type_hook

    @property
    def _tag_value(self) -> str:
        return str(self.type)
