# pyright: reportPrivateUsage=false

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Self

from common.types import isinstance_or_raise

_RESOURCE_LOCATION_RE = re.compile(r"(?:([0-9a-z_\-.]+):)?([0-9a-z_\-./]+)")
_ITEM_STACK_SUFFIX_RE = re.compile(r"(?:#([0-9]+))?({.*})?")
_ENTITY_SUFFIX_RE = re.compile(r"({.*})?")


def _match(
    pat: re.Pattern[str],
    fullmatch: bool,
    string: str,
    pos: int = 0,
) -> re.Match[str] | None:
    if fullmatch:
        return pat.fullmatch(string, pos)
    return pat.match(string, pos)


@dataclass(repr=False, frozen=True)
class BaseResourceLocation:
    """Represents a Minecraft resource location / namespaced ID."""

    namespace: str
    path: str

    @classmethod
    def _parse_str(
        cls,
        raw: str,
        fullmatch: bool = True,
    ) -> tuple[tuple[Any, ...], re.Match[str]]:
        assert isinstance_or_raise(raw, str)

        match = _match(_RESOURCE_LOCATION_RE, fullmatch, raw)
        if match is None:
            raise ValueError(f"invalid resource location: {raw}")

        namespace, path = match.groups()
        if namespace is None:
            namespace = "minecraft"

        return (namespace, path), match

    @classmethod
    def from_str(cls, raw: str | Self) -> Self:
        if isinstance(raw, BaseResourceLocation):
            return raw
        parts, _ = cls._parse_str(raw, fullmatch=True)
        return cls(*parts)

    def __repr__(self) -> str:
        return f"{self.namespace}:{self.path}"


@dataclass(repr=False, frozen=True)
class ResourceLocation(BaseResourceLocation):
    @classmethod
    def from_file(cls, modid: str, base_dir: Path, path: Path) -> ResourceLocation:
        resource_path = path.relative_to(base_dir).with_suffix("").as_posix()
        return ResourceLocation(modid, resource_path)

    @property
    def href(self) -> str:
        return f"#{self.path}"


# pure unadulterated laziness
ResLoc = ResourceLocation


@dataclass(repr=False, frozen=True)
class ItemStack(BaseResourceLocation):
    """Represents an item with optional count and NBT tags.

    Does not inherit from ResourceLocation.
    """

    count: int | None = None
    nbt: str | None = None

    @classmethod
    def _parse_str(
        cls,
        raw: str,
        fullmatch: bool = True,
    ) -> tuple[tuple[Any, ...], re.Match[str]]:
        rl_parts, rl_match = super()._parse_str(raw, fullmatch=False)

        match = _match(_ITEM_STACK_SUFFIX_RE, fullmatch, raw, rl_match.end())
        if match is None:
            raise ValueError(f"invalid ItemStack String: {raw}")

        count, nbt = match.groups()
        if count is not None:
            count = int(count)

        return rl_parts + (count, nbt), match

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation(self.namespace, self.path)

    def i18n_key(self, root: str = "item") -> str:
        return f"{root}.{self.namespace}.{self.path}"

    def __repr__(self) -> str:
        s = str(self.id)
        if self.count is not None:
            s += f"#{self.count}"
        if self.nbt is not None:
            s += self.nbt
        return s


@dataclass(repr=False, frozen=True)
class Entity(BaseResourceLocation):
    """Represents an entity with optional NBT.

    Does not inherit from ResourceLocation.
    """

    nbt: str | None = None

    @classmethod
    def _parse_str(
        cls,
        raw: str,
        fullmatch: bool = True,
    ) -> tuple[tuple[Any, ...], re.Match[str]]:
        rl_parts, rl_match = super()._parse_str(raw, fullmatch=False)

        match = _match(_ENTITY_SUFFIX_RE, fullmatch, raw, rl_match.end())
        if match is None:
            raise ValueError(f"invalid Entity: {raw}")

        return rl_parts + (match[1],), match

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation(self.namespace, self.path)

    def __repr__(self) -> str:
        s = str(self.id)
        if self.nbt is not None:
            s += self.nbt
        return s
