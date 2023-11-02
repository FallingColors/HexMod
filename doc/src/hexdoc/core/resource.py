# this file is used by basically everything
# so if it's in literally any other place, everything dies from circular deps
# basically, just leave it here

from __future__ import annotations

import logging
import re
from fnmatch import fnmatch
from pathlib import Path
from typing import Any, ClassVar, Literal, Self

from pydantic import TypeAdapter, field_validator, model_serializer, model_validator
from pydantic.dataclasses import dataclass
from pydantic.functional_validators import ModelWrapValidatorHandler

from hexdoc.model import DEFAULT_CONFIG

ResourceType = Literal["assets", "data", ""]


def _make_regex(count: bool = False, nbt: bool = False) -> re.Pattern[str]:
    pattern = r"(?:(?P<namespace>[0-9a-z_\-.]+):)?(?P<path>[0-9a-z_\-./*]+)"
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
    def from_str(cls, raw: str) -> Self:
        match = cls._from_str_regex.fullmatch(raw)
        if match is None:
            raise ValueError(f"Invalid {cls.__name__} string: {raw}")

        return cls(**match.groupdict())

    @classmethod
    def model_validate(cls, value: Any, *, context: Any = None):
        ta = TypeAdapter(cls)
        return ta.validate_python(value, context=context)

    @model_validator(mode="wrap")
    @classmethod
    def _pre_root(cls, values: Any, handler: ModelWrapValidatorHandler[Self]):
        # before validating the fields, if it's a string instead of a dict, convert it
        logging.getLogger(__name__).debug(f"Convert {values} to {cls.__name__}")
        if isinstance(values, str):
            return cls.from_str(values)
        return handler(values)

    @field_validator("namespace", mode="before")
    def _default_namespace(cls, value: Any):
        match value:
            case str():
                return value.lower()
            case None:
                return "minecraft"
            case _:
                return value

    @field_validator("path")
    def _validate_path(cls, value: str):
        return value.lower().rstrip("/")

    @model_serializer
    def _ser_model(self) -> str:
        return str(self)

    @property
    def id(self) -> ResourceLocation:
        return ResourceLocation(self.namespace, self.path)

    def __repr__(self) -> str:
        return f"{self.namespace}:{self.path}"


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False)
class ResourceLocation(BaseResourceLocation, regex=_make_regex()):
    """Represents a Minecraft resource location / namespaced ID."""

    is_tag: bool = False

    @classmethod
    def from_str(cls, raw: str) -> Self:
        id = super().from_str(raw.removeprefix("#"))
        if raw.startswith("#"):
            object.__setattr__(id, "is_tag", True)
        return id

    @classmethod
    def from_file(cls, modid: str, base_dir: Path, path: Path) -> Self:
        resource_path = path.relative_to(base_dir).with_suffix("").as_posix()
        return ResourceLocation(modid, resource_path)

    @property
    def href(self) -> str:
        return f"#{self.path}"

    @property
    def class_name(self):
        stripped_path = re.sub(r"[\*\/\.]", "-", self.path)
        return f"texture-{self.namespace}-{stripped_path}"

    def with_namespace(self, namespace: str):
        """Returns a copy of this ResourceLocation with the given namespace."""
        return ResourceLocation(namespace, self.path)

    def with_path(self, path: str | Path):
        """Returns a copy of this ResourceLocation with the given path."""
        if isinstance(path, Path):
            path = path.as_posix()
        return ResourceLocation(self.namespace, path)

    def match(self, pattern: Self) -> bool:
        return fnmatch(str(self), str(pattern))

    def file_path_stub(
        self,
        type: ResourceType,
        folder: str | Path = "",
        assume_json: bool = True,
    ) -> Path:
        """Returns the path to find this resource within a resource directory.

        If `assume_json` is True and no file extension is provided, `.json` is assumed.

        For example:
        ```py
        ResLoc("hexcasting", "thehexbook/book").file_path_stub("data", "patchouli_books")
        # data/hexcasting/patchouli_books/thehexbook/book.json
        ```
        """
        # if folder is an empty string, Path won't add an extra slash
        path = Path(type) / self.namespace / folder / self.path
        if assume_json and not path.suffix:
            return path.with_suffix(".json")
        return path

    def __truediv__(self, other: str) -> Self:
        return ResourceLocation(self.namespace, f"{self.path}/{other}")

    def __repr__(self) -> str:
        s = super().__repr__()
        if self.is_tag:
            return f"#{s}"
        return s


# pure unadulterated laziness
ResLoc = ResourceLocation


@dataclass(config=DEFAULT_CONFIG, frozen=True, repr=False)
class ItemStack(BaseResourceLocation, regex=_make_regex(count=True, nbt=True)):
    """Represents an item with optional count and NBT.

    Inherits from BaseResourceLocation, not ResourceLocation.
    """

    count: int | None = None
    nbt: str | None = None

    def __init_subclass__(cls, **kwargs: Any):
        super().__init_subclass__(regex=cls._from_str_regex, **kwargs)

    def i18n_key(self, root: str = "item") -> str:
        # TODO: is this how i18n works????? (apparently, because it's working)
        return f"{root}.{self.namespace}.{self.path.replace('/', '.')}"

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
