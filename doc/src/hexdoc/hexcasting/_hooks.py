from importlib.resources import Package

from hexdoc.__gradle_version__ import GRADLE_VERSION
from hexdoc._export import generated, resources
from hexdoc.plugin import (
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    ModVersionImpl,
    hookimpl,
)

from . import hex_recipes
from .page import hex_pages


class HexcastingPlugin(LoadResourceDirsImpl, LoadTaggedUnionsImpl, ModVersionImpl):
    @staticmethod
    @hookimpl
    def hexdoc_mod_version() -> str:
        return GRADLE_VERSION

    @staticmethod
    @hookimpl
    def hexdoc_load_resource_dirs() -> Package | list[Package]:
        return [generated, resources]

    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> Package | list[Package]:
        return [hex_recipes, hex_pages]
