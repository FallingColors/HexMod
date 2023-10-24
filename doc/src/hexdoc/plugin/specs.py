from importlib.resources import Package
from typing import Any, Protocol, TypeVar

import pluggy

HEXDOC_PROJECT_NAME = "hexdoc"


_T = TypeVar("_T")

HookReturn = _T | list[_T]

HookReturns = list[HookReturn[_T]]


hookspec = pluggy.HookspecMarker(HEXDOC_PROJECT_NAME)


class PluginSpec(Protocol):
    @staticmethod
    @hookspec(firstresult=True)
    def hexdoc_mod_version() -> str | None:
        ...

    @staticmethod
    @hookspec
    def hexdoc_update_template_args(*, template_args: dict[str, Any]) -> None:
        ...

    @staticmethod
    @hookspec
    def hexdoc_load_resource_dirs() -> HookReturns[Package]:
        ...

    @staticmethod
    @hookspec
    def hexdoc_load_tagged_unions() -> HookReturns[Package]:
        ...

    @staticmethod
    @hookspec
    def hexdoc_load_jinja_templates() -> HookReturns[tuple[Package, str]]:
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


class ExtraTemplateArgsImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_update_template_args`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_update_template_args(template_args: dict[str, Any]) -> None:
        ...


class LoadResourceDirsImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_load_resource_dirs`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_load_resource_dirs() -> HookReturn[Package]:
        """Return the module(s) which contain your plugin's exported book resources."""
        ...


class LoadTaggedUnionsImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_load_tagged_unions`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_load_tagged_unions() -> HookReturn[Package]:
        """Return the module(s) which contain your plugin's tagged union subtypes."""
        ...


class LoadJinjaTemplatesImpl(Protocol):
    """Interface for a plugin implementing `hexdoc_load_jinja_templates`.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """

    @staticmethod
    def hexdoc_load_jinja_templates() -> HookReturn[tuple[Package, str]]:
        ...
