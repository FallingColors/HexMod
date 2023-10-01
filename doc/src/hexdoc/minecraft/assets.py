import logging
from typing import Self

import requests
from pydantic import Field, ValidationInfo, model_validator
from pydantic.dataclasses import dataclass

from hexdoc.minecraft.i18n import I18nContext, LocalizedStr
from hexdoc.utils import ItemStack, ResourceLocation
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.model import DEFAULT_CONFIG
from hexdoc.utils.resource_loader import resolve_texture_from_metadata

# 16x16 hashtag icon for tags
TAG_TEXTURE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAC4jAAAuIwF4pT92AAAANUlEQVQ4y2NgGJRAXV39v7q6+n9cfGTARKllFBvAiOxMUjTevHmTkSouGPhAHA0DWnmBrgAANLIZgSXEQxIAAAAASUVORK5CYII="

MISSING_TEXTURE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAAC4jAAAuIwF4pT92AAAAJElEQVQoz2NkwAF+MPzAKs7EQCIY1UAMYMQV3hwMHKOhRD8NAPogBA/DVsDEAAAAAElFTkSuQmCC"


class MinecraftAssetsContext(I18nContext):
    minecraft_textures: dict[ResourceLocation, str | None] = Field(default_factory=dict)

    @model_validator(mode="after")
    def _fetch_minecraft_textures(self) -> Self:
        asset_props = self.props.minecraft_assets
        url = (
            "https://raw.githubusercontent.com/PrismarineJS/minecraft-assets"
            f"/{asset_props.ref}/data/{asset_props.version}/texture_content.json"
        )

        logging.getLogger(__name__).info(f"Fetch textures from {url}")
        resp = requests.get(url)
        resp.raise_for_status()

        textures_list: list[dict[{"name": str, "texture": str | None}]] = resp.json()

        for item in textures_list:
            id = ResourceLocation("minecraft", item["name"])
            # items are first in the list (i think), so prioritize them
            if id not in self.minecraft_textures:
                self.minecraft_textures[id] = item["texture"]

        return self


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False, kw_only=True)
class RenderedItemStack(ItemStack):
    name: LocalizedStr | None = None
    texture: str | None = None

    @model_validator(mode="after")
    def _add_name_and_texture(self, info: ValidationInfo):
        """Loads the recipe from json if the actual value is a resource location str."""
        if not info.context:
            return self
        context = cast_or_raise(info.context, MinecraftAssetsContext)

        object.__setattr__(self, "name", context.i18n.localize_item(self))
        object.__setattr__(self, "texture", self._get_texture(context))

        return self

    def _get_texture(self, context: MinecraftAssetsContext):
        if self.namespace == "minecraft":
            return context.minecraft_textures[self.id]
        else:
            # check if there's an override to apply
            id = context.props.textures.override.get(self.id, self.id)

            try:
                return resolve_texture_from_metadata(
                    context.loader.mod_metadata,
                    id.with_path(f"textures/item/{id.path}.png"),
                    id.with_path(f"textures/block/{id.path}.png"),
                )

            except KeyError as e:
                for missing_id in context.props.textures.missing:
                    if id.match(missing_id):
                        logging.getLogger(__name__).warn(f"No texture for {id}")
                        return MISSING_TEXTURE
                raise KeyError(f"No texture for {id}") from e
