from __future__ import annotations

from importlib.resources import Package
from typing import TYPE_CHECKING, Any, Protocol, TypeVar

import pluggy
from jinja2.sandbox import SandboxedEnvironment

if TYPE_CHECKING:
    from hexdoc.core.resource import ResourceLocation
    from hexdoc.minecraft import I18n
    from hexdoc.patchouli.text import FormatTree

HEXDOC_PROJECT_NAME = "hexdoc"

hookspec = pluggy.HookspecMarker(HEXDOC_PROJECT_NAME)


_T = TypeVar("_T")

HookReturn = _T | list[_T]

HookReturns = list[HookReturn[_T]]


class PluginSpec(Protocol):
    @staticmethod
    @hookspec(firstresult=True)
    def hexdoc_mod_version() -> str | None:
        ...

    @staticmethod
    @hookspec
    def hexdoc_validate_format_tree(
        tree: FormatTree,
        macros: dict[str, str],
        i18n: I18n,
        book_id: ResourceLocation,
        is_0_black: bool,
    ) -> None:
        ...

    @staticmethod
    @hookspec
    def hexdoc_update_jinja_env(env: SandboxedEnvironment) -> None:
        ...

    @staticmethod
    @hookspec
    def hexdoc_update_template_args(template_args: dict[str, Any]) -> None:
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


class PluginImpl(Protocol):
    """Interface for an implementation of a hexdoc plugin hook.

    These protocols are optional - they gives better type checking, but everything will
    work fine with a standard pluggy hook implementation.
    """


class ModVersionImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_mod_version() -> str:
        """Return your plugin's mod version (ie. `GRADLE_VERSION`)."""
        ...


class ValidateFormatTreeImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_validate_format_tree(
        tree: FormatTree,
        macros: dict[str, str],
        book_id: ResourceLocation,
        i18n: I18n,
        is_0_black: bool,
    ) -> None:
        ...


class UpdateJinjaEnvImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_update_jinja_env(env: SandboxedEnvironment) -> None:
        ...


class UpdateTemplateArgsImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_update_template_args(template_args: dict[str, Any]) -> None:
        ...


class LoadResourceDirsImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_load_resource_dirs() -> HookReturn[Package]:
        """Return the module(s) which contain your plugin's exported book resources."""
        ...


class LoadTaggedUnionsImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_load_tagged_unions() -> HookReturn[Package]:
        """Return the module(s) which contain your plugin's tagged union subtypes."""
        ...


class LoadJinjaTemplatesImpl(PluginImpl, Protocol):
    @staticmethod
    def hexdoc_load_jinja_templates() -> HookReturn[tuple[Package, str]]:
        ...
