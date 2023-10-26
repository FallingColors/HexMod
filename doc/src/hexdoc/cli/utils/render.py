# pyright: reportUnknownMemberType=false, reportUnknownArgumentType=false
# pyright: reportUnknownLambdaType=false

import logging
import shutil
from pathlib import Path
from typing import Any

from jinja2 import ChoiceLoader, PrefixLoader, StrictUndefined, Template
from jinja2.sandbox import SandboxedEnvironment

from hexdoc.core.metadata import HexdocMetadata
from hexdoc.core.properties import Properties
from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import I18n
from hexdoc.minecraft.assets.textures import AnimatedTexture, Texture
from hexdoc.patchouli import Book
from hexdoc.plugin import PluginManager
from hexdoc.utils.jinja.extensions import IncludeRawExtension
from hexdoc.utils.jinja.filters import hexdoc_localize, hexdoc_texture, hexdoc_wrap
from hexdoc.utils.path import write_to_path

from .sitemap import MARKER_NAME, SitemapMarker


def create_jinja_env(pm: PluginManager, include: list[str]):
    prefix_loaders = pm.load_jinja_templates(include)

    env = SandboxedEnvironment(
        loader=ChoiceLoader(
            [
                PrefixLoader(prefix_loaders, ":"),
                *prefix_loaders.values(),
            ]
        ),
        undefined=StrictUndefined,
        lstrip_blocks=True,
        trim_blocks=True,
        autoescape=True,
        extensions=[
            IncludeRawExtension,
        ],
    )

    env.filters |= {  # pyright: ignore[reportGeneralTypeIssues]
        "hexdoc_wrap": hexdoc_wrap,
        "hexdoc_localize": hexdoc_localize,
        "hexdoc_texture": hexdoc_texture,
    }

    return pm.update_jinja_env(env)


def render_book(
    *,
    props: Properties,
    pm: PluginManager,
    lang: str,
    lang_names: dict[str, str],
    book: Book,
    i18n: I18n,
    templates: dict[Path, Template],
    output_dir: Path,
    all_metadata: dict[str, HexdocMetadata],
    textures: dict[ResourceLocation, Texture],
    animations: list[AnimatedTexture],
    allow_missing: bool,
    version: str,
    is_root: bool,
):
    # /index.html
    # /lang/index.html
    # /v/version/index.html
    # /v/version/lang/index.html
    path = Path()
    if not is_root:
        path /= "v"
        path /= version
    if lang != props.default_lang:
        path /= lang

    output_dir /= path
    page_url = "/".join([props.url, *path.parts])

    logging.getLogger(__name__).info(f"Rendering {output_dir}")

    template_args: dict[str, Any] = {
        "book": book,
        "props": props,
        "i18n": i18n,
        "page_url": page_url,
        "version": version,
        "lang": lang,
        "lang_names": lang_names,
        "all_metadata": all_metadata,
        "textures": textures,
        "animations": animations,
        "is_bleeding_edge": version == "latest",
        "link_bases": book.link_bases,
        "_": lambda key: hexdoc_localize(  # i18n helper
            key,
            do_format=False,
            props=props,
            book=book,
            i18n=i18n,
            allow_missing=allow_missing,
        ),
        "_f": lambda key: hexdoc_localize(  # i18n helper with patchi formatting
            key,
            do_format=True,
            props=props,
            book=book,
            i18n=i18n,
            allow_missing=allow_missing,
        ),
        **props.template.args,
    }
    pm.update_template_args(template_args)

    for filename, template in templates.items():
        file = template.render(template_args)
        stripped_file = strip_empty_lines(file)
        write_to_path(output_dir / filename, stripped_file)

    if props.template.static_dir:
        shutil.copytree(props.template.static_dir, output_dir, dirs_exist_ok=True)

    # marker file for updating the sitemap later
    # we use this because matrix doesn't have outputs
    # this feels scuffed but it does work
    if not is_root:
        marker = SitemapMarker(
            version=version,
            lang=lang,
            lang_name=lang_names[lang],
            path="/" + "/".join(path.parts),
            is_default_lang=lang == props.default_lang,
        )
        (output_dir / MARKER_NAME).write_text(marker.model_dump_json(), "utf-8")


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())
