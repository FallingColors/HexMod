import logging
from typing import TypedDict

import requests


class MinecraftAssetsTextureContent(TypedDict):
    name: str
    texture: str | None


def fetch_minecraft_textures(
    *,
    ref: str,
    version: str,
) -> list[MinecraftAssetsTextureContent]:
    url = (
        "https://raw.githubusercontent.com/PrismarineJS/minecraft-assets"
        f"/{ref}/data/{version}/texture_content.json"
    )

    logging.getLogger(__name__).info(f"Fetch textures from {url}")
    resp = requests.get(url)
    resp.raise_for_status()

    return resp.json()
