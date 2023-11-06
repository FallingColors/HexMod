import re
from importlib.resources import Package
from pathlib import Path

from hexdoc.core import ResourceLocation
from hexdoc.minecraft import I18n
from hexdoc.patchouli import BookContext, FormatTree
from hexdoc.patchouli.text import STYLE_REGEX, SpecialStyleType, Style, resolve_macros
from hexdoc.plugin import (
    DefaultRenderedTemplatesImpl,
    HookReturn,
    LoadJinjaTemplatesImpl,
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    MinecraftVersionImpl,
    ModVersionImpl,
    UpdateContextImpl,
    ValidateFormatTreeImpl,
    hookimpl,
)
from hexdoc.utils import relative_path_root

import hexdoc_hexcasting

from .__gradle_version__ import GRADLE_VERSION, MINECRAFT_VERSION
from .book import recipes
from .book.page import pages
from .metadata import HexContext, HexProperties

# TODO: make this more extensible somehow
FAKE_ACTIONS = {
    "Demoman's Gambit",
}


def check_action_links(tree: FormatTree, in_link: bool, action_style: Style):
    if (
        not in_link
        and tree.style == action_style
        and not any(action in tree.children for action in FAKE_ACTIONS)
    ):
        raise ValueError(f"Action style missing link: {tree}")

    if tree.style.type == SpecialStyleType.link:
        in_link = True

    for child in tree.children:
        if isinstance(child, FormatTree):
            check_action_links(child, in_link, action_style)


class HexcastingPlugin(
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    LoadJinjaTemplatesImpl,
    ModVersionImpl,
    ValidateFormatTreeImpl,
    MinecraftVersionImpl,
    UpdateContextImpl,
    DefaultRenderedTemplatesImpl,
):
    @staticmethod
    @hookimpl
    def hexdoc_mod_version() -> str:
        return GRADLE_VERSION

    @staticmethod
    @hookimpl
    def hexdoc_minecraft_version() -> str:
        return MINECRAFT_VERSION

    @staticmethod
    @hookimpl
    def hexdoc_load_resource_dirs() -> HookReturn[Package]:
        # lazy import because generated may not exist when this file is loaded
        # eg. when generating the contents of generated
        # so we only want to import it if we actually need it
        from hexdoc_hexcasting._export import generated

        return generated

    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> HookReturn[Package]:
        return [recipes, pages]

    @staticmethod
    @hookimpl
    def hexdoc_load_jinja_templates() -> HookReturn[tuple[Package, str]]:
        return hexdoc_hexcasting, "_templates"

    @staticmethod
    @hookimpl
    def hexdoc_default_rendered_templates(templates: dict[str | Path, str]) -> None:
        templates.update(
            {
                "hexcasting.js": "hexcasting.js.jinja",
            }
        )

    @staticmethod
    @hookimpl
    def hexdoc_update_context(context: BookContext) -> None:
        with relative_path_root(context.props.props_dir):
            hex_props = HexProperties.model_validate(context.props.extra["hexcasting"])

        hex_context = HexContext(hex_props=hex_props)
        hex_context.load_patterns(context)

        context.extra["hexcasting"] = hex_context

    @staticmethod
    @hookimpl
    def hexdoc_validate_format_tree(
        tree: FormatTree,
        macros: dict[str, str],
        book_id: ResourceLocation,
        i18n: I18n,
        is_0_black: bool,
    ):
        if "$(action)" not in macros:
            return

        match = re.search(STYLE_REGEX, resolve_macros("$(action)", macros))
        if not match:
            return

        action_style = Style.parse(match[1], book_id, i18n, is_0_black)
        if isinstance(action_style, Style):
            check_action_links(tree, False, action_style)
