import importlib
from dataclasses import dataclass
from importlib.resources import Package
from types import ModuleType
from typing import Callable, Generic, Iterator, ParamSpec, Sequence, TypeVar

import pluggy

from hexdoc.utils.model import ValidationContext

from .specs import HEXDOC_PROJECT_NAME, HookPackages, PluginSpec

_T = TypeVar("_T")

_P = ParamSpec("_P")
_R = TypeVar("_R", covariant=True)


@dataclass
class TypedHookCaller(Generic[_P, _R]):
    plugin_name: str | None
    caller: pluggy.HookCaller

    @property
    def name(self):
        return self.caller.name

    @property
    def plugin_display_name(self):
        if self.plugin_name is None:
            return HEXDOC_PROJECT_NAME

        return f"Plugin {HEXDOC_PROJECT_NAME}-{self.plugin_name}"

    def __call__(self, *args: _P.args, **kwargs: _P.kwargs) -> _R:
        result = self.caller(*args, **kwargs)
        match result:
            case None | []:
                raise RuntimeError(
                    f"{self.plugin_display_name} does not implement hook {self.name}"
                )
            case _:
                return result


class PluginManager:
    """Custom hexdoc plugin manager with helpers and stronger typing."""

    def __init__(self) -> None:
        self.inner = pluggy.PluginManager(HEXDOC_PROJECT_NAME)
        self.inner.add_hookspecs(PluginSpec)
        self.inner.load_setuptools_entrypoints(HEXDOC_PROJECT_NAME)
        self.inner.check_pending()

    def mod_version(self, modid: str):
        return self._hook_caller(modid, PluginSpec.hexdoc_mod_version)()

    def load_resources(self, modid: str) -> Iterator[ModuleType]:
        yield from self._import_from_hook(modid, PluginSpec.hexdoc_load_resource_dirs)

    def load_tagged_unions(self, modid: str | None = None) -> Iterator[ModuleType]:
        yield from self._import_from_hook(modid, PluginSpec.hexdoc_load_tagged_unions)

    def _import_from_hook(
        self,
        __modid: str | None,
        __spec: Callable[_P, HookPackages],
        *args: _P.args,
        **kwargs: _P.kwargs,
    ) -> Iterator[ModuleType]:
        packages = self._hook_caller(__modid, __spec)(*args, **kwargs)
        for package in flatten(packages):
            yield import_package(package)

    def _hook_caller(
        self,
        modid: str | None,
        spec: Callable[_P, _R | None],
    ) -> TypedHookCaller[_P, _R]:
        """Returns a hook caller for the named method which only manages calls to a
        specific modid (aka plugin name)."""

        caller = self.inner.subset_hook_caller(
            spec.__name__,
            remove_plugins=()
            if modid is None
            else (
                plugin
                for name, plugin in self.inner.list_name_plugin()
                if name != modid
            ),
        )

        return TypedHookCaller(modid, caller)


class PluginManagerContext(ValidationContext, arbitrary_types_allowed=True):
    pm: PluginManager


def flatten(values: list[list[_T] | _T]) -> Iterator[_T]:
    for value in values:
        if isinstance(value, Sequence) and not isinstance(value, (str, bytes)):
            yield from value
        else:
            yield value


def import_package(package: Package) -> ModuleType:
    match package:
        case ModuleType():
            return package
        case str():
            return importlib.import_module(package)
