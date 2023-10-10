from pathlib import Path
from typing import Self

from pydantic import model_validator

from hexdoc.minecraft.assets import Texture, TextureContext
from hexdoc.model import HexdocModel

from .properties import NoTrailingSlashHttpUrl
from .resource import ResourceLocation


class HexdocMetadata(HexdocModel):
    """Automatically generated at `export_dir/modid.hexdoc.json`."""

    book_url: NoTrailingSlashHttpUrl
    """Github Pages base url."""
    asset_url: NoTrailingSlashHttpUrl
    """raw.githubusercontent.com base url."""
    textures: dict[ResourceLocation, Texture]
    """id -> path from repo root"""

    @classmethod
    def path(cls, modid: str) -> Path:
        return Path(f"{modid}.hexdoc.json")


class MetadataContext(TextureContext):
    all_metadata: dict[str, HexdocMetadata]

    @model_validator(mode="after")
    def _add_metadata_textures(self) -> Self:
        for metadata in self.all_metadata.values():
            self.textures |= metadata.textures
        return self
