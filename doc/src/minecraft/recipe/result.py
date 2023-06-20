from dataclasses import dataclass
from typing import Any

from common.types import LocalizedItem


@dataclass(kw_only=True)
class BlockState:
    name: LocalizedItem
    properties: dict[str, Any] | None = None


@dataclass
class ItemResult:
    item: LocalizedItem
    count: int | None = None
