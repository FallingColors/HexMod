from abc import ABC, abstractmethod
from typing import Any, Literal, Self

from hexdoc.core import IsVersion, ResourceLocation
from hexdoc.minecraft.assets import ItemWithTexture, PNGTexture, TextureI18nContext
from hexdoc.minecraft.i18n import LocalizedStr
from hexdoc.minecraft.recipe import ItemIngredient, ItemIngredientList, Recipe
from hexdoc.model import HexdocModel, TypeTaggedTemplate
from hexdoc.utils import NoValue, cast_or_raise, classproperty
from pydantic import Field, PrivateAttr, ValidationInfo, model_validator

# ingredients


class BrainsweepeeIngredient(TypeTaggedTemplate, type=None):
    @classproperty
    @classmethod
    def template(cls):
        # template_id is actually supposed to just be a string
        # but pydantic generics are hard :(
        return f"ingredients/hexcasting/brainsweepee/{cls.template_id.path}"


# lol, lmao
class VillagerIngredient(BrainsweepeeIngredient, type="villager"):
    min_level: int = Field(alias="minLevel")
    profession: ResourceLocation = ResourceLocation("minecraft", "none")
    biome: ResourceLocation | None = None

    _level_name: LocalizedStr = PrivateAttr()
    _profession_name: LocalizedStr = PrivateAttr()
    _texture: PNGTexture = PrivateAttr()

    @property
    def level_name(self):
        return self._level_name

    @property
    def profession_name(self):
        return self._profession_name

    @property
    def texture(self):
        return self._texture

    @model_validator(mode="after")
    def _get_texture(self, info: ValidationInfo) -> Self:
        context: TextureI18nContext = cast_or_raise(info.context, TextureI18nContext)
        i18n = context.i18n

        self._level_name = i18n.localize(f"merchant.level.{self.min_level}")

        self._profession_name = i18n.localize_entity(self.profession, "villager")

        self._texture = PNGTexture.load_id(
            id="textures/entities/villagers" / self.profession + ".png",
            context=context,
        )

        return self


@IsVersion("<1.20")
class VillagerIngredient_0_10(
    VillagerIngredient,
    type=NoValue,
    template_type="villager",
):
    pass


@IsVersion(">=1.20")
class EntityTypeIngredient(BrainsweepeeIngredient, type="entity_type"):
    entity_type: ResourceLocation = Field(alias="entityType")

    _name: LocalizedStr = PrivateAttr()
    _texture: PNGTexture = PrivateAttr()

    @property
    def name(self):
        return self._name

    @property
    def texture(self):
        return self._texture

    @model_validator(mode="after")
    def _get_texture(self, info: ValidationInfo) -> Self:
        context: TextureI18nContext = cast_or_raise(info.context, TextureI18nContext)

        self._name = context.i18n.localize_entity(self.entity_type)

        self._texture = PNGTexture.load_id(
            id="textures/entities" / self.entity_type + ".png",
            context=context,
        )

        return self


@IsVersion(">=1.20")
class EntityTagIngredient(BrainsweepeeIngredient, type="entity_tag"):
    tag: ResourceLocation


class BlockStateIngredient(HexdocModel):
    # TODO: tagged union
    type: Literal["block"]
    block: ItemWithTexture


class ModConditionalIngredient(ItemIngredient, type="hexcasting:mod_conditional"):
    default: ItemIngredientList
    if_loaded: ItemIngredientList
    modid: str


# results


class BlockState(HexdocModel):
    name: ItemWithTexture
    properties: dict[str, Any] | None = None


# recipes


class BrainsweepRecipe(Recipe, ABC, type=None):
    blockIn: BlockStateIngredient
    result: BlockState

    @property
    @abstractmethod
    def brainsweepee(self) -> Any:
        """Returns the object representing this recipe's brainsweepee.

        For example, `BrainsweepRecipe_0_11` returns `entityIn`.
        """


@IsVersion(">=1.20")
class BrainsweepRecipe_0_11(BrainsweepRecipe, type="hexcasting:brainsweep"):
    cost: int
    entityIn: BrainsweepeeIngredient

    @property
    def brainsweepee(self):
        return self.entityIn


@IsVersion("<1.20")
class BrainsweepRecipe_0_10(BrainsweepRecipe, type="hexcasting:brainsweep"):
    villagerIn: VillagerIngredient_0_10

    @property
    def brainsweepee(self):
        return self.villagerIn
