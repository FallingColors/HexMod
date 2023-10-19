from importlib.resources import Package

import hexdoc
from hexdoc.__gradle_version__ import GRADLE_VERSION
from hexdoc.plugin import (
    HookReturn,
    LoadJinjaTemplatesImpl,
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    ModVersionImpl,
    hookimpl,
)

from . import hex_recipes
from .page import hex_pages


class HexcastingPlugin(
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    ModVersionImpl,
    LoadJinjaTemplatesImpl,
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
