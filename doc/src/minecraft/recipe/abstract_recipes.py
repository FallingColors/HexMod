from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Self

from common.deserialize import TypeHook, load_json_data
from common.state import AnyState, TypeTaggedUnion
from common.types import LocalizedItem
from minecraft.resource import ResourceLocation


@dataclass
class ItemIngredientData:
    item: ResourceLocation | None = None
    tag: ResourceLocation | None = None


ItemIngredient = ItemIngredientData | list[ItemIngredientData]


@dataclass
class ItemResult:
    item: LocalizedItem
    count: int | None = None


@dataclass(kw_only=True)
class Recipe(TypeTaggedUnion[AnyState], type=None):
    id: ResourceLocation
    group: str | None = None

    @classmethod
    def make_type_hook(cls, state: AnyState) -> TypeHook[Self]:
        """Creates a type hook which, given a stringified ResourceLocation, loads and
        returns the recipe json at that location."""
        super_hook = super().make_type_hook(state)

        def type_hook(data: str | Any) -> Self | dict[str, Any]:
            if isinstance(data, str):
                # FIXME: hack
                # the point of this is to ensure the recipe exists on all platforms
                # because we've had issues with that in the past, eg. in Hexal
                id = ResourceLocation.from_str(data)
                data = {}
                for recipe_dir in state.props.recipe_dirs:
                    # TODO: should this use id.namespace somewhere?
                    path = recipe_dir / f"{id.path}.json"
                    data = load_json_data(cls, path, {"id": id})

            return super_hook(data)

        return type_hook


@dataclass(kw_only=True)
class CraftingRecipe(Recipe[AnyState], type=None):
    result: ItemResult
