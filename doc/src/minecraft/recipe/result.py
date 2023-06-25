from dataclasses import dataclass

from common.types import LocalizedItem


@dataclass
class ItemResult:
    item: LocalizedItem
    count: int | None = None
