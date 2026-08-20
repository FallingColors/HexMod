from typing import Any, Literal, Self

from hexdoc.core import I18n, ItemStack, LocalizedStr, ResourceLocation
from hexdoc.graphics import ImageField, ItemImage, TextureImage
from hexdoc.minecraft.recipe import ItemIngredient, ItemIngredientList, Recipe
from hexdoc.model import HexdocModel, TypeTaggedTemplate
from hexdoc.utils import classproperty
from pydantic import Field, PrivateAttr, TypeAdapter, ValidationInfo, model_validator

from hexdoc_hexcasting.utils.constants import (
    MEDIA_CRYSTAL_UNIT,
    MEDIA_DUST_UNIT,
    MEDIA_SHARD_UNIT,
)

# ingredients


class BrainsweepeeIngredient(TypeTaggedTemplate, type=None):
    @classproperty
    @classmethod
    def template(cls):
        # template_id is actually supposed to just be a string
        # but pydantic generics are hard :(
        return f"ingredients/hexcasting/brainsweepee/{cls.template_id.path}"


# lol, lmao
class VillagerIngredient(BrainsweepeeIngredient, type="hexcasting:villager"):
    min_level: int = Field(alias="minLevel")
    profession: ResourceLocation = ResourceLocation("minecraft", "none")
    biome: ResourceLocation | None = None

    _level_name: LocalizedStr = PrivateAttr()
    _profession_name: LocalizedStr = PrivateAttr()
    _image: TextureImage = PrivateAttr()

    @property
    def level_name(self):
        return self._level_name

    @property
    def profession_name(self):
        return self._profession_name

    @property
    def image(self):
        return self._image

    @model_validator(mode="after")
    def _get_image(self, info: ValidationInfo) -> Self:
        assert info.context is not None
        i18n = I18n.of(info)

        self._level_name = i18n.localize(f"merchant.level.{self.min_level}")

        self._profession_name = i18n.localize_entity(self.profession, "villager")

        self._image = TypeAdapter(ImageField[TextureImage]).validate_python(
            "textures/entities/villagers" / self.profession + ".png",
            context=info.context,
        )

        return self


class EntityTypeIngredient(BrainsweepeeIngredient, type="hexcasting:entity_type"):
    entity_type: ResourceLocation = Field(alias="entityType")

    _name: LocalizedStr = PrivateAttr()
    _image: TextureImage = PrivateAttr()

    @property
    def name(self):
        return self._name

    @property
    def image(self):
        return self._image

    @model_validator(mode="after")
    def _get_image(self, info: ValidationInfo) -> Self:
        assert info.context is not None
        i18n = I18n.of(info)

        self._name = i18n.localize_entity(self.entity_type)

        self._image = TypeAdapter(ImageField[TextureImage]).validate_python(
            "textures/entities" / self.entity_type + ".png",
            context=info.context,
        )

        return self


class EntityTagIngredient(BrainsweepeeIngredient, type="hexcasting:entity_tag"):
    tag: ResourceLocation


class BlockStateIngredient(HexdocModel):
    # TODO: tagged union
    type: Literal["hexcasting:block"]
    block: ImageField[ItemImage]


class ModConditionalIngredient(ItemIngredient, type="hexcasting:mod_conditional"):
    default: ItemIngredientList
    if_loaded: ItemIngredientList
    modid: str


# results


class BlockState(HexdocModel):
    Name: ImageField[ItemImage]
    Properties: dict[str, Any] | None = None


# recipes


class BrainsweepRecipe(Recipe, type="hexcasting:brainsweep"):
    block_in: BlockStateIngredient = Field(alias="blockIn")
    entity_in: BrainsweepeeIngredient = Field(alias="entityIn")
    cost: int
    result: BlockState

    @property
    def cost_items(self) -> list[ItemStack]:
        """Returns the items to display for the recipe's cost."""

        costs = [
            ("hexcasting", "amethyst_dust", MEDIA_DUST_UNIT),
            ("minecraft", "amethyst_shard", MEDIA_SHARD_UNIT),
            ("hexcasting", "charged_amethyst", MEDIA_CRYSTAL_UNIT),
        ]

        return [
            ItemStack(namespace, path, self.cost // media)
            for namespace, path, media in costs
            if self.cost % media == 0
        ] or [
            # fallback if nothing divides evenly
            ItemStack("hexcasting", "amethyst_dust", self.cost // MEDIA_DUST_UNIT),
        ]
