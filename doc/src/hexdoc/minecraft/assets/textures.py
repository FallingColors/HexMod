from __future__ import annotations

import logging
from functools import cached_property
from pathlib import Path
from typing import Literal, Self

from pydantic import Field, model_validator

from hexdoc.minecraft.i18n import I18nContext, LocalizedStr
from hexdoc.utils import HexdocModel, ResourceLocation
from hexdoc.utils.external import fetch_minecraft_textures
from hexdoc.utils.properties import Properties
from hexdoc.utils.resource_loader import ModResourceLoader
from hexdoc.utils.resource_model import InlineModel

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
            texture = "/" + path.resolve().relative_to(root).as_posix()
            meta_path = path.with_suffix(".png.mcmeta")

            if meta_path.is_file():
                yield AnimatedTexture(
                    file_id=id,
                    url=texture,
                    meta=AnimationMeta.model_validate_json(meta_path.read_bytes()),
                )
            else:
                yield Texture(file_id=id, url=texture)

    @classmethod
    def load_id(cls, id: ResourceLocation, context: TextureContext):
        """Implements InlineModel."""
        return cls.find(id, props=context.props, textures=context.textures)

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
            percent -= 0.0001
        return f"{percent:.4f}".rstrip("0").rstrip(".")


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


class WithTexture(InlineModel):
    id: ResourceLocation
    name: LocalizedStr
    texture: Texture


class ItemWithTexture(WithTexture):
    @classmethod
    def load_id(cls, id: ResourceLocation, context: TextureContext):
        """Implements InlineModel."""
        texture_id = context.props.textures.override.get(id, id)
        return cls(
            id=id,
            name=context.i18n.localize_item(id),
            texture=Texture.find(
                texture_id,
                texture_id.with_path(f"item/{texture_id.path}.png"),
                texture_id.with_path(f"block/{texture_id.path}.png"),
                props=context.props,
                textures=context.textures,
            ),
        )


class TagWithTexture(WithTexture):
    @classmethod
    def load_id(cls, id: ResourceLocation, context: TextureContext):
        return cls(
            id=id,
            name=context.i18n.localize_item_tag(id),
            texture=Texture(file_id=id, url=TAG_TEXTURE),
        )
