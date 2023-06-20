from dataclasses import dataclass, field
from typing import Any, Literal, Self

from common.deserialize import TypeFn, load_json_data
from common.tagged_union import InternallyTaggedUnion
from common.types import Book, LocalizedItem
from minecraft.resource import ResourceLocation

# ingredients/results


@dataclass
class ItemIngredientData:
    item: ResourceLocation | None = None
    tag: ResourceLocation | None = None


# TODO: tagged union~!
@dataclass
class ModConditionalIngredient:
    type: Literal["hexcasting:mod_conditional"]
    default: ItemIngredientData
    if_loaded: ItemIngredientData
    modid: str


ItemIngredient = (
    ItemIngredientData | ModConditionalIngredient | list[ItemIngredientData]
)


@dataclass
class VillagerIngredient:
    minLevel: int
    profession: ResourceLocation | None = None
    biome: ResourceLocation | None = None


@dataclass(kw_only=True)
class BlockState:
    name: LocalizedItem
    properties: dict[str, Any] | None = None


@dataclass
class BlockStateIngredient:
    # TODO: StateIngredient should also be a TypeTaggedUnion, probably
    type: Literal["block"]
    block: ResourceLocation


@dataclass
class ItemResult:
    item: LocalizedItem
    count: int | None = None


# recipe types


@dataclass(kw_only=True)
class BaseRecipe(InternallyTaggedUnion, tag="type", value=None):
    id: ResourceLocation

    type: ResourceLocation = field(init=False)
    group: str | None = None

    def __init_subclass__(cls, type: str) -> None:
        super().__init_subclass__(__class__._tag_name, type)
        cls.type = ResourceLocation.from_str(type)

    @classmethod
    def make_type_hook(cls, book: Book) -> TypeFn:
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


@dataclass
class CraftingShapedRecipe(BaseRecipe, type="minecraft:crafting_shaped"):
    pattern: list[str]
    key: dict[str, ItemIngredient]
    result: ItemResult


@dataclass
class CraftingShapelessRecipe(BaseRecipe, type="minecraft:crafting_shapeless"):
    ingredients: list[ItemIngredient]
    result: ItemResult


@dataclass
class BrainsweepRecipe(BaseRecipe, type="hexcasting:brainsweep"):
    blockIn: BlockStateIngredient
    villagerIn: VillagerIngredient
    result: BlockState


CraftingRecipe = CraftingShapedRecipe | CraftingShapelessRecipe

Recipe = CraftingShapedRecipe | CraftingShapelessRecipe | BrainsweepRecipe
