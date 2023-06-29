# pyright: reportPrivateUsage=false

from __future__ import annotations

import re
from abc import ABC, abstractmethod
from pathlib import Path
from typing import Any, Self

from pydantic import field_validator, model_validator, validator
from pydantic.dataclasses import dataclass

from common.deserialize import DEFAULT_CONFIG
from common.types import isinstance_or_raise


def _make_re(count: bool = False, nbt: bool = False) -> re.Pattern[str]:
    pattern = r"(?:([0-9a-z_\-.]+):)?([0-9a-z_\-./]+)"
    if count:
        pattern += r"(?:#([0-9]+))?"
    if nbt:
        pattern += r"({.*})?"
    return re.compile(pattern)


_RESOURCE_LOCATION_RE = _make_re()
_ITEM_STACK_RE = _make_re(count=True, nbt=True)
_ENTITY_RE = _make_re(nbt=True)


@dataclass(config=DEFAULT_CONFIG, repr=False, frozen=True)
class BaseResourceLocation(ABC):
    """Represents a Minecraft resource location / namespaced ID."""

    namespace: str
    path: str

    @classmethod  # TODO: model_validator
    def from_str(cls, raw: Self | str) -> Self:
        if isinstance(raw, BaseResourceLocation):
            return raw
        return cls(*cls._match_groups(raw))

    @classmethod
    def _match_groups(cls, raw: str) -> tuple[str, ...]:
        assert isinstance_or_raise(raw, str)  # TODO: remove

        match = cls._fullmatch(raw)
        if match is None:
            raise ValueError(f"Invalid {cls.__name__} string: {raw}")

        namespace, *rest = match.groups()
        return (namespace or "minecraft", *rest)

    @classmethod
    @abstractmethod
    def _fullmatch(cls, string: str) -> re.Match[str] | None:
        ...

    def __repr__(self) -> str:
        return f"{self.namespace}:{self.path}"


@dataclass(config=DEFAULT_CONFIG, repr=False, frozen=True)
class ResourceLocation(BaseResourceLocation):
    @classmethod
    def _fullmatch(cls, string: str) -> re.Match[str] | None:
        return _RESOURCE_LOCATION_RE.fullmatch(string)

    @classmethod
    def from_file(cls, modid: str, base_dir: Path, path: Path) -> ResourceLocation:
        resource_path = path.relative_to(base_dir).with_suffix("").as_posix()
        return ResourceLocation(modid, resource_path)

    @property
    def href(self) -> str:
        return f"#{self.path}"


# pure unadulterated laziness
ResLoc = ResourceLocation


@dataclass(config=DEFAULT_CONFIG, repr=False, frozen=True)
class ItemStack(BaseResourceLocation):
    """Represents an item with optional count and NBT.

    Inherits from BaseResourceLocation, not ResourceLocation.
    """

    count: int | None = None
    nbt: str | None = None

    @field_validator("count", mode="before")  # TODO: move this into _match_groups?
    def convert_count(cls, count: str | int | None):
        if isinstance(count, str):
            return int(count)
        return count

    @classmethod
    def _fullmatch(cls, string: str) -> re.Match[str] | None:
        return _ITEM_STACK_RE.fullmatch(string)

    def i18n_key(self, root: str = "item") -> str:
        return f"{root}.{self.namespace}.{self.path}"

    def __repr__(self) -> str:
        s = super().__repr__()
        if self.count is not None:
            s += f"#{self.count}"
        if self.nbt is not None:
            s += self.nbt
        return s


@dataclass(repr=False, frozen=True)
class Entity(BaseResourceLocation):
    """Represents an entity with optional NBT.

    Inherits from BaseResourceLocation, not ResourceLocation.
    """

    nbt: str | None = None

    @classmethod
    def _fullmatch(cls, string: str) -> re.Match[str] | None:
        return _ENTITY_RE.fullmatch(string)

    def __repr__(self) -> str:
        s = super().__repr__()
        if self.nbt is not None:
            s += self.nbt
        return s
