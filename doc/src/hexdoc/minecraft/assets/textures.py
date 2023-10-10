from __future__ import annotations

import logging
from functools import cached_property
from pathlib import Path
from typing import Literal, Self

from pydantic import Field, model_validator

from hexdoc.core.loader import ModResourceLoader
from hexdoc.core.properties import Properties
from hexdoc.core.resource import ItemStack, ResourceLocation
from hexdoc.model import HexdocModel
from hexdoc.model.inline import InlineItemModel, InlineModel

from ..i18n import I18nContext, LocalizedStr
from .external import fetch_minecraft_textures

# 16x16 hashtag icon for tags
TAG_TEXTURE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAC4jAAAuIwF4pT92AAAANUlEQVQ4y2NgGJRAXV39v7q6+n9cfGTARKllFBvAiOxMUjTevHmTkSouGPhAHA0DWnmBrgAANLIZgSXEQxIAAAAASUVORK5CYII="

# purple and black square
MISSING_TEXTURE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAAC4jAAAuIwF4pT92AAAAJElEQVQoz2NkwAF+MPzAKs7EQCIY1UAMYMQV3hwMHKOhRD8NAPogBA/DVsDEAAAAAElFTkSuQmCC"


class Texture(InlineModel):
    file_id: ResourceLocation
    url: str | None
    meta: None = None

    @classmethod
    def load_all(cls, root: Path, loader: ModResourceLoader):
        for _, id, path in loader.find_resources(
            "assets",
            namespace=loader.props.modid,
            folder="textures",
            glob=f"**/*.png",
        ):
            relative_path = path.resolve().relative_to(root)
            url = f"{loader.props.env.asset_url}/{relative_path.as_posix()}"

            meta_path = path.with_suffix(".png.mcmeta")
            if meta_path.is_file():
                yield AnimatedTexture(
                    file_id=id,
                    url=url,
                    meta=AnimationMeta.model_validate_json(meta_path.read_bytes()),
                )
            else:
                yield Texture(file_id=id, url=url)

    @classmethod
    def load_id(cls, id: ResourceLocation, context: TextureContext):
        return cls.find(id, props=context.props, textures=context.textures)

    @classmethod
    def find_item(
        cls,
        id: ResourceLocation,
        props: Properties,
        textures: dict[ResourceLocation, Texture],
    ):
        return cls.find(
            id,
            id.with_path(f"item/{id.path}.png"),
            id.with_path(f"block/{id.path}.png"),
            props=props,
            textures=textures,
        )

    @classmethod
    def find(
        cls,
        *ids: ResourceLocation,
        props: Properties,
        textures: dict[ResourceLocation, Texture],
    ):
        for id in ids:
            id = id.with_path(id.path.removeprefix("textures/"))
            if id in textures:
                return textures[id]

        # fallback/error
        message = f"No texture for {', '.join(str(i) for i in ids)}"

        for missing_id in props.textures.missing:
            for id in ids:
                if id.match(missing_id):
                    logging.getLogger(__name__).warn(message)
                    return Texture(file_id=id, url=MISSING_TEXTURE)

        raise KeyError(message)


class AnimatedTexture(Texture):
    meta: AnimationMeta

    @property
    def class_name(self):
        return self.file_id.class_name

    @property
    def time_seconds(self):
        return self.time / 20

    @cached_property
    def time(self):
        return sum(time for _, time in self._normalized_frames)

    @property
    def frames(self):
        start = 0
        for index, time in self._normalized_frames:
            yield AnimatedTextureFrame(
                index=index,
                start=start,
                time=time,
                animation_time=self.time,
            )
            start += time

    @property
    def _normalized_frames(self):
        """index, time"""
        animation = self.meta.animation

        for i, frame in enumerate(animation.frames):
            match frame:
                case int(index):
                    time = None
                case AnimationMetaFrame(index=index, time=time):
                    pass

            if index is None:
                index = i
            if time is None:
                time = animation.frametime

            yield index, time


class AnimatedTextureFrame(HexdocModel):
    index: int
    start: int
    time: int
    animation_time: int

    @property
    def start_percent(self):
        return self._format_time(self.start)

    @property
    def end_percent(self):
        return self._format_time(self.start + self.time, backoff=True)

    def _format_time(self, time: int, *, backoff: bool = False) -> str:
        percent = 100 * time / self.animation_time
        if backoff and percent < 100:
            percent -= 0.01
        return f"{percent:.2f}".rstrip("0").rstrip(".")


class AnimationMeta(HexdocModel):
    animation: AnimationMetaTag


class AnimationMetaTag(HexdocModel):
    interpolate: Literal[False]  # TODO: handle interpolation
    width: None = None  # TODO: handle non-square textures
    height: None = None
    frametime: int = 1
    frames: list[int | AnimationMetaFrame]


class AnimationMetaFrame(HexdocModel):
    index: int | None = None
    time: int | None = None


class TextureContext(I18nContext):
    textures: dict[ResourceLocation, Texture] = Field(default_factory=dict)

    @model_validator(mode="after")
    def _add_minecraft_textures(self) -> Self:
        minecraft_textures = fetch_minecraft_textures(
            ref=self.props.minecraft_assets.ref,
            version=self.props.minecraft_assets.version,
        )

        for item in minecraft_textures:
            id = ResourceLocation("minecraft", item["name"])
            # prefer items, since they're added first
            if id not in self.textures:
                self.textures[id] = Texture(file_id=id, url=item["texture"])

        return self


class ItemWithTexture(InlineItemModel):
    id: ItemStack
    name: LocalizedStr
    texture: Texture

    @classmethod
    def load_id(cls, item: ItemStack, context: TextureContext):
        """Implements InlineModel."""
        try:
            return ItemWithGaslightingTexture.load_id(item, context)
        except KeyError:
            texture_id = context.props.textures.override.get(item.id, item.id)
            return cls(
                id=item,
                name=context.i18n.localize_item(item),
                texture=Texture.find_item(texture_id, context.props, context.textures),
            )

    @property
    def gaslighting(self):
        return False


class ItemWithGaslightingTexture(InlineItemModel):
    id: ItemStack
    name: LocalizedStr
    textures: list[Texture]

    @classmethod
    def load_id(cls, item: ItemStack, context: TextureContext):
        """Implements InlineModel."""
        gaslighting = context.props.textures.gaslighting[item.id]
        return cls(
            id=item,
            name=context.i18n.localize_item(item),
            textures=[
                Texture.find_item(
                    id=ResourceLocation.from_str(gaslighting.id.format(index)),
                    props=context.props,
                    textures=context.textures,
                )
                for index in range(gaslighting.variants)
            ],
        )

    @property
    def gaslighting(self):
        return True


class TagWithTexture(InlineModel):
    id: ResourceLocation
    name: LocalizedStr
    texture: Texture

    @classmethod
    def load_id(cls, id: ResourceLocation, context: TextureContext):
        return cls(
            id=id,
            name=context.i18n.localize_item_tag(id),
            texture=Texture(file_id=id, url=TAG_TEXTURE),
        )

    @property
    def gaslighting(self):
        return False
