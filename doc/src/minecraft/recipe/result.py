from dataclasses import dataclass
from typing import Any

from common.types import LocalizedItem


@dataclass
class ItemResult:
    item: LocalizedItem
    count: int | None = None
