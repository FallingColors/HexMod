from importlib.resources import Package
from typing import Protocol

import pluggy

HEXDOC_PROJECT_NAME = "hexdoc"

hookspec = pluggy.HookspecMarker(HEXDOC_PROJECT_NAME)


HookPackages = list[Package | list[Package]]


class PluginSpec(Protocol):
    @staticmethod
    @hookspec(firstresult=True)
    def hexdoc_mod_version() -> str | None:
        ...

    @staticmethod
    @hookspec
    def hexdoc_load_resource_dirs() -> HookPackages:
        ...

    @staticmethod
    @hookspec
    def hexdoc_load_tagged_unions() -> HookPackages:
        ...


# mmmmmm, interfaces


class ModVersionImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_mod_version`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_mod_version() -> str:
        """Return your plugin's mod version (ie. `GRADLE_VERSION`)."""
        ...


class LoadResourceDirsImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_load_resource_dirs`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_load_resource_dirs() -> Package | list[Package]:
        """Return the module(s) which contain your plugin's exported book resources."""
        ...


class LoadTaggedUnionsImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_load_tagged_unions`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_load_tagged_unions() -> Package | list[Package]:
        """Return the module(s) which contain your plugin's tagged union subtypes."""
        ...
