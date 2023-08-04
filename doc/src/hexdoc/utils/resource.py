# pyright: reportPrivateUsage=false

# this file is used by basically everything
# so if it's in literally any namespace, everything fucking dies from circular deps
# basically, just leave it here

import re
from pathlib import Path
from typing import Any, ClassVar, Self

from pydantic import field_validator, model_serializer, model_validator
from pydantic.dataclasses import dataclass
from pydantic.functional_validators import ModelWrapValidatorHandler

from .model import DEFAULT_CONFIG


def _make_regex(count: bool = False, nbt: bool = False) -> re.Pattern[str]:
    pattern = r"(?:(?P<namespace>[0-9a-z_\-.]+):)?(?P<path>[0-9a-z_\-./]+)"
    if count:
        pattern += r"(?:#(?P<count>[0-9]+))?"
    if nbt:
        pattern += r"(?P<nbt>{.*})?"
    return re.compile(pattern)


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False)
class BaseResourceLocation:
    namespace: str
    path: str

    _from_str_regex: ClassVar[re.Pattern[str]]

    def __init_subclass__(cls, regex: re.Pattern[str]) -> None:
        cls._from_str_regex = regex

    @classmethod
    def from_str(cls, raw: str, default_namespace: str | None = None) -> Self:
        match = cls._from_str_regex.fullmatch(raw)
        if match is None:
            raise ValueError(f"Invalid {cls.__name__} string: {raw}")

        groups = match.groupdict()
        if not groups.get("namespace") and default_namespace is not None:
            groups["namespace"] = default_namespace

        return cls(**groups)

    @model_validator(mode="wrap")
    @classmethod
    def _pre_root(cls, values: str | Any, handler: ModelWrapValidatorHandler[Self]):
        # before validating the fields, if it's a string instead of a dict, convert it
        if isinstance(values, str):
            return cls.from_str(values)
        return handler(values)

    @field_validator("namespace", mode="before")
    def _default_namespace(cls, value: str | None) -> str:
        if value is None:
            return "minecraft"
        return value

    @model_serializer
    def _ser_model(self) -> str:
        return str(self)

    @property
    def full_path(self) -> str:
        return f"{self.namespace}/{self.path}"

    def __repr__(self) -> str:
        return f"{self.namespace}:{self.path}"


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False)
class ResourceLocation(BaseResourceLocation, regex=_make_regex()):
    """Represents a Minecraft resource location / namespaced ID."""

    @classmethod
    def from_file(cls, modid: str, base_dir: Path, path: Path) -> Self:
        resource_path = path.relative_to(base_dir).with_suffix("").as_posix()
        return ResourceLocation(modid, resource_path)

    @property
    def href(self) -> str:
        return f"#{self.path}"


# pure unadulterated laziness
ResLoc = ResourceLocation


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False)
class ItemStack(BaseResourceLocation, regex=_make_regex(count=True, nbt=True)):
    """Represents an item with optional count and NBT.

    Inherits from BaseResourceLocation, not ResourceLocation.
    """

    count: int | None = None
    nbt: str | None = None

    def i18n_key(self, root: str = "item") -> str:
        return f"{root}.{self.namespace}.{self.path}"

    def __repr__(self) -> str:
        s = super().__repr__()
        if self.count is not None:
            s += f"#{self.count}"
        if self.nbt is not None:
            s += self.nbt
        return s


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False)
class Entity(BaseResourceLocation, regex=_make_regex(nbt=True)):
    """Represents an entity with optional NBT.

    Inherits from BaseResourceLocation, not ResourceLocation.
    """

    nbt: str | None = None

    def __repr__(self) -> str:
        s = super().__repr__()
        if self.nbt is not None:
            s += self.nbt
        return s
