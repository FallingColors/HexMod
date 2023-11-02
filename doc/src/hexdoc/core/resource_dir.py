# pyright: reportUnknownArgumentType=false, reportUnknownMemberType=false

from __future__ import annotations

from abc import ABC, abstractmethod
from contextlib import ExitStack, contextmanager
from pathlib import Path
from typing import Any, ContextManager, Iterable, Self

import importlib_resources as resources
from pydantic import ValidationInfo, field_validator, model_validator

from hexdoc.model import HexdocModel
from hexdoc.plugin import PluginManager
from hexdoc.utils.cd import RelativePath, relative_path_root


class BaseResourceDir(HexdocModel, ABC):
    external: bool
    reexport: bool
    """If not set, the default value will be `not self.external`.
    
    Must be defined AFTER `external` in the Pydantic model.
    """

    @abstractmethod
    def load(
        self,
        pm: PluginManager,
    ) -> ContextManager[Iterable[PathResourceDir]]:
        ...

    @field_validator("reexport", mode="before")
    def _default_reexport(cls, value: Any, info: ValidationInfo):
        if value is None and "external" in info.data:
            return not info.data["external"]
        return value


class PathResourceDir(BaseResourceDir):
    # input is relative to the props file
    path: RelativePath

    # direct paths are probably from this mod
    external: bool = False
    reexport: bool = True

    # not a props field
    _modid: str | None = None

    @property
    def modid(self):
        return self._modid

    @property
    def internal(self):
        return not self.external

    def set_modid(self, modid: str) -> Self:
        self._modid = modid
        return self

    @contextmanager
    def load(self, pm: PluginManager):
        yield [self]

    @model_validator(mode="before")
    def _pre_root(cls: Any, value: Any):
        # treat plain strings as paths
        if isinstance(value, str):
            return {"path": value}
        return value


class PluginResourceDir(BaseResourceDir):
    modid: str

    # if we're specifying a modid, it's probably from some other mod/package
    external: bool = True
    reexport: bool = False

    @contextmanager
    def load(self, pm: PluginManager):
        with ExitStack() as stack, relative_path_root(Path()):
            yield list(self._load_all(pm, stack))  # NOT "yield from"

    def _load_all(self, pm: PluginManager, stack: ExitStack):
        for module in pm.load_resources(self.modid):
            traversable = resources.files(module)
            path = stack.enter_context(resources.as_file(traversable))

            yield PathResourceDir(
                path=path,
                external=self.external,
                reexport=self.reexport,
            ).set_modid(self.modid)


ResourceDir = PathResourceDir | PluginResourceDir
