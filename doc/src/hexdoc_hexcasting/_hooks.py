import re
from importlib.resources import Package
from pathlib import Path

from hexdoc.core import ResourceLocation
from hexdoc.minecraft import I18n
from hexdoc.patchouli import BookContext, FormatTree
from hexdoc.patchouli.text import STYLE_REGEX, SpecialStyleType, Style, resolve_macros
from hexdoc.plugin import (
    HookReturn,
    LoadTaggedUnionsImpl,
    ModPlugin,
    ModPluginImpl,
    ModPluginWithBook,
    UpdateContextImpl,
    ValidateFormatTreeImpl,
    hookimpl,
)
from hexdoc.utils import relative_path_root
from typing_extensions import override

import hexdoc_hexcasting

from .__gradle_version__ import FULL_VERSION, GRADLE_VERSION, MINECRAFT_VERSION
from .__version__ import PY_VERSION
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
    LoadTaggedUnionsImpl,
    ValidateFormatTreeImpl,
    UpdateContextImpl,
    ModPluginImpl,
):
    @staticmethod
    @hookimpl
    def hexdoc_mod_plugin(branch: str) -> ModPlugin:
        return HexcastingModPlugin(branch=branch)

    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> HookReturn[Package]:
        return [recipes, pages]

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
        # the values in FAKE_ACTIONS are from the default language, ie. en_us
        # so just skip this validation for other languages
        # (TODO: hack)
        if "$(action)" not in macros or not i18n.is_default:
            return

        match = re.search(STYLE_REGEX, resolve_macros("$(action)", macros))
        if not match:
            return

        action_style = Style.parse(match[1], book_id, i18n, is_0_black)
        if isinstance(action_style, Style):
            check_action_links(tree, False, action_style)


class HexcastingModPlugin(ModPluginWithBook):
    @property
    @override
    def modid(self) -> str:
        return "hexcasting"

    @property
    @override
    def full_version(self) -> str:
        return FULL_VERSION

    @property
    @override
    def mod_version(self) -> str:
        return GRADLE_VERSION

    @property
    @override
    def plugin_version(self) -> str:
        return PY_VERSION

    @property
    @override
    def compat_minecraft_version(self) -> str:
        return MINECRAFT_VERSION

    @override
    def resource_dirs(self) -> HookReturn[Package]:
        # lazy import because generated may not exist when this file is loaded
        # eg. when generating the contents of generated
        # so we only want to import it if we actually need it
        from hexdoc_hexcasting._export import generated

        return generated

    @override
    def jinja_template_root(self) -> tuple[Package, str]:
        return hexdoc_hexcasting, "_templates"

    @override
    def default_rendered_templates(self) -> dict[str | Path, str]:
        return {
            "hexcasting.js": "hexcasting.js.jinja",
        }
