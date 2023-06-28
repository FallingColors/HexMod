from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Self

from common.deserialize import load_json_data
from common.types import LocalizedItem
from minecraft.resource import ResourceLocation
from patchouli.state import AnyState, StatefulTypeTaggedUnion


@dataclass
class ItemResult:
    item: LocalizedItem
    count: int | None = None


@dataclass(kw_only=True)
class Recipe(StatefulTypeTaggedUnion[AnyState], group="hexdoc.Recipe", type=None):
    id: ResourceLocation
    group: str | None = None

    @classmethod
    def stateful_type_hook(cls, data: Self | Any, state: AnyState) -> Self:
        # if it's a resourcelocation, fetch the data in the corresponding recipe file
        if isinstance(data, (str, ResourceLocation)):
            id = ResourceLocation.from_str(data)

            # FIXME: hack
            # this is to ensure the recipe exists on all platforms, because we've had
            # issues with that in the past (eg. Hexal's Mote Nexus)
            data = {}
            for recipe_dir in state.props.recipe_dirs:
                # TODO: should this use id.namespace somewhere?
                path = recipe_dir / f"{id.path}.json"
                data = load_json_data(cls, path, {"id": id})

        return super().stateful_type_hook(data, state)
