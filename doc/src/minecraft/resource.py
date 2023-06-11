import re
from dataclasses import dataclass
from typing import Any, Self

from serde import field

_RESOURCE_LOCATION_RE = re.compile(r"(?:([0-9a-z_\-.]+):)?([0-9a-z_\-./]+)")
_ITEM_STACK_SUFFIX_RE = re.compile(r"(?:#([0-9]+))?({.*})?")


# TODO: instead of the dataclass field thing, make this subclass str
# _namespace and _method, access via properties
@dataclass(repr=False, frozen=True)
class ResourceLocation:
    """Represents a Minecraft resource location / namespaced ID."""

    namespace: str
    path: str

    @classmethod
    def from_str(cls, s: str) -> Self:
        match = _RESOURCE_LOCATION_RE.fullmatch(s)
        if match is None:
            raise ValueError(f"invalid resource location: {s}")

        namespace, path = match.groups()
        if namespace is None:
            namespace = "minecraft"

        return cls(namespace, path)

    @classmethod
    def field(cls, s: str | None = None, **kwargs: Any) -> Any:
        """Helper method for using this as a dataclass field. You must use this method if
        you're putting this in a serde class.

        s may be a raw resource location string to construct a default value from.
        """
        if s is not None:
            kwargs["default_factory"] = cls.from_str(s)
        return field(deserializer=cls.from_str, **kwargs)

    def __repr__(self) -> str:
        return f"{self.namespace}:{self.path}"


@dataclass(repr=False, frozen=True)
class ItemStack(ResourceLocation):
    """Represents an item with optional count and NBT tags."""

    count: int | None = None
    nbt: str | None = None

    @classmethod
    def from_str(cls, s: str) -> Self:
        resource_match = _RESOURCE_LOCATION_RE.match(s)
        if resource_match is None:
            raise ValueError(f"invalid ItemStack String: {s}")

        resource = ResourceLocation.from_str(resource_match[0])

        match = _ITEM_STACK_SUFFIX_RE.fullmatch(s, resource_match.end())
        if match is None:
            raise ValueError(f"invalid ItemStack String: {s}")

        count, nbt = match.groups()
        if count is not None:
            count = int(count)

        return cls(resource.namespace, resource.path, count, nbt)

    def __repr__(self) -> str:
        s = super().__repr__()
        if self.count is not None:
            s += f"#{self.count}"
        if self.nbt is not None:
            s += self.nbt
        return s
