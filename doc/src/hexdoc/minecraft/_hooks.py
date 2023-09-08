from importlib.resources import Package

from hexdoc.plugin import LoadTaggedUnionsImpl, hookimpl

from .recipe import ingredients, recipes


class MinecraftPlugin(LoadTaggedUnionsImpl):
    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> Package | list[Package]:
        return [ingredients, recipes]
