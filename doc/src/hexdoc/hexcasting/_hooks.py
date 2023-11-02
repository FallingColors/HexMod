import re
from importlib.resources import Package

import hexdoc
from hexdoc.__gradle_version__ import GRADLE_VERSION
from hexdoc.minecraft.i18n import I18n
from hexdoc.patchouli.text import (
    STYLE_REGEX,
    FormatTree,
    SpecialStyleType,
    Style,
    resolve_macros,
)
from hexdoc.plugin import (
    HookReturn,
    LoadJinjaTemplatesImpl,
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    ModVersionImpl,
    hookimpl,
)

from ..core.resource import ResourceLocation
from ..plugin.specs import ValidateFormatTreeImpl
from . import hex_recipes
from .page import hex_pages

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
):
    @staticmethod
    @hookimpl
    def hexdoc_mod_version() -> str:
        return GRADLE_VERSION

    @staticmethod
    @hookimpl
    def hexdoc_load_resource_dirs() -> HookReturn[Package]:
        # lazy import because generated may not exist when this file is loaded
        # eg. when generating the contents of generated
        # so we only want to import it if we actually need it
        from hexdoc._export import generated

        return generated

    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> HookReturn[Package]:
        return [hex_recipes, hex_pages]

    @staticmethod
    @hookimpl
    def hexdoc_load_jinja_templates() -> HookReturn[tuple[Package, str]]:
        return hexdoc, "_templates"

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
