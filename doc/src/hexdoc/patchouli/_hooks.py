from importlib.resources import Package

from hexdoc.plugin import LoadTaggedUnionsImpl, hookimpl

from .page import pages


class PatchouliPlugin(LoadTaggedUnionsImpl):
    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> Package | list[Package]:
        return pages
