from __future__ import annotations

import importlib
from dataclasses import dataclass
from importlib.resources import Package
from types import ModuleType
from typing import (
    Any,
    Callable,
    Generic,
    Iterable,
    Iterator,
    Never,
    ParamSpec,
    TypeVar,
    overload,
)

import pluggy
from jinja2 import PackageLoader
from jinja2.sandbox import SandboxedEnvironment

from hexdoc.model import ValidationContext

from .specs import HEXDOC_PROJECT_NAME, HookReturns, PluginSpec

_T = TypeVar("_T")

_P = ParamSpec("_P")
_R = TypeVar("_R", covariant=True)


class PluginNotFoundError(RuntimeError):
    pass


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

    def try_call(self, *args: _P.args, **kwargs: _P.kwargs) -> _R | None:
        result = self.caller(*args, **kwargs)
        match result:
            case None | []:
                return None
            case _:
                return result

    def __call__(self, *args: _P.args, **kwargs: _P.kwargs) -> _R:
        result = self.try_call(*args, **kwargs)
        if result is None:
            raise PluginNotFoundError(
                f"{self.plugin_display_name} does not implement hook {self.name}"
            )
        return result


class _NoCallTypedHookCaller(TypedHookCaller[_P, None]):
    """Represents a TypedHookCaller which returns None. This will always raise, so the
    return type of __call__ is set to Never."""

    def __call__(self, *args: _P.args, **kwargs: _P.kwargs) -> Never:
        ...


class PluginManager:
    """Custom hexdoc plugin manager with helpers and stronger typing."""

    def __init__(self) -> None:
        self.inner = pluggy.PluginManager(HEXDOC_PROJECT_NAME)
        self.inner.add_hookspecs(PluginSpec)
        self.inner.load_setuptools_entrypoints(HEXDOC_PROJECT_NAME)
        self.inner.check_pending()

    def mod_version(self, modid: str):
        return self._hook_caller(PluginSpec.hexdoc_mod_version, modid)()

    def update_jinja_env(self, env: SandboxedEnvironment):
        caller = self._hook_caller(PluginSpec.hexdoc_update_jinja_env)
        caller.try_call(env=env)
        return env

    def update_template_args(self, template_args: dict[str, Any]):
        caller = self._hook_caller(PluginSpec.hexdoc_update_template_args)
        caller.try_call(template_args=template_args)
        return template_args

    def load_resources(self, modid: str) -> Iterator[ModuleType]:
        yield from self._import_from_hook(PluginSpec.hexdoc_load_resource_dirs, modid)

    def load_tagged_unions(self, modid: str | None = None) -> Iterator[ModuleType]:
        yield from self._import_from_hook(PluginSpec.hexdoc_load_tagged_unions, modid)

    def load_jinja_templates(self, modids: Iterable[str]):
        """modid -> PackageLoader"""
        loaders = dict[str, PackageLoader]()
        for modid in modids:
            caller = self._hook_caller(PluginSpec.hexdoc_load_jinja_templates, modid)
            for package, package_path in flatten(caller()):
                module = import_package(package)
                loaders[modid] = PackageLoader(module.__name__, package_path)
        return loaders

    def _import_from_hook(
        self,
        __spec: Callable[_P, HookReturns[Package]],
        __modid: str | None = None,
        *args: _P.args,
        **kwargs: _P.kwargs,
    ) -> Iterator[ModuleType]:
        packages = self._hook_caller(__spec, __modid)(*args, **kwargs)
        for package in flatten(packages):
            yield import_package(package)

    def _all_hook_callers(
        self,
        spec: Callable[_P, _R | None],
    ) -> Iterator[tuple[str, TypedHookCaller[_P, _R]]]:
        for modid, plugin in self.inner.list_name_plugin():
            caller = self.inner.subset_hook_caller(spec.__name__, [plugin])
            yield modid, TypedHookCaller(modid, caller)

    @overload
    def _hook_caller(
        self,
        spec: Callable[_P, None],
        modid: str | None = None,
    ) -> _NoCallTypedHookCaller[_P]:
        ...

    @overload
    def _hook_caller(
        self,
        spec: Callable[_P, _R | None],
        modid: str | None = None,
    ) -> TypedHookCaller[_P, _R]:
        ...

    def _hook_caller(
        self,
        spec: Callable[_P, _R | None],
        modid: str | None = None,
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
        if isinstance(value, list):
            yield from value
        else:
            yield value


def import_package(package: Package) -> ModuleType:
    match package:
        case ModuleType():
            return package
        case str():
            return importlib.import_module(package)
