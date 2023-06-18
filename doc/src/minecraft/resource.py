# pyright: reportPrivateUsage=false

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Self

_RESOURCE_LOCATION_RE = re.compile(r"(?:([0-9a-z_\-.]+):)?([0-9a-z_\-./]+)")
_ITEM_STACK_SUFFIX_RE = re.compile(r"(?:#([0-9]+))?({.*})?")


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
class ResourceLocation:
    """Represents a Minecraft resource location / namespaced ID."""

    namespace: str
    path: str

    _match_end: int = field(default=0, kw_only=True, compare=False)

    @classmethod
    def from_str(cls, raw: str, fullmatch: bool = True) -> Self:
        assert isinstance(raw, str), f"Expected str, got {type(raw)}"

        match = _match(_RESOURCE_LOCATION_RE, fullmatch, raw)
        if match is None:
            raise ValueError(f"invalid resource location: {raw}")

        namespace, path = match.groups()
        if namespace is None:
            namespace = "minecraft"

        return cls(namespace, path, _match_end=match.end())

    @classmethod
    def from_file(cls, modid: str, base_dir: Path, path: Path) -> ResourceLocation:
        resource_path = path.relative_to(base_dir).with_suffix("").as_posix()
        return ResourceLocation(modid, resource_path)

    @property
    def href(self) -> str:
        return f"#{self.path}"

    def __repr__(self) -> str:
        return f"{self.namespace}:{self.path}"


# pure unadulterated laziness
ResLoc = ResourceLocation


@dataclass(repr=False, frozen=True)
class ItemStack:
    """Represents an item with optional count and NBT tags.

    Does not inherit from ResourceLocation.
    """

    namespace: str
    path: str
    count: int | None = None
    nbt: str | None = None

    _match_end: int = field(default=0, kw_only=True, compare=False)

    @classmethod
    def from_str(cls, raw: str, fullmatch: bool = True) -> Self:
        id = ResourceLocation.from_str(raw, fullmatch=False)

        match = _match(_ITEM_STACK_SUFFIX_RE, fullmatch, raw, id._match_end)
        if match is None:
            raise ValueError(f"invalid ItemStack String: {raw}")

        count, nbt = match.groups()
        if count is not None:
            count = int(count)

        return cls(id.namespace, id.path, count, nbt, _match_end=match.end())

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
