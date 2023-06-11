from abc import ABC, abstractmethod
from typing import Any, Mapping, TypeVar


class Sortable(ABC):
    """ABC for classes which can be sorted."""

    @property
    @abstractmethod
    def cmp_key(self) -> Any:
        ...

    def __lt__(self, other: Any) -> bool:
        if isinstance(other, Sortable):
            return self.cmp_key < other.cmp_key
        return NotImplemented


_T = TypeVar("_T")
_T_Sortable = TypeVar("_T_Sortable", bound=Sortable)


def sorted_dict(d: Mapping[_T, _T_Sortable]) -> dict[_T, _T_Sortable]:
    return dict(sorted(d.items(), key=lambda item: item[1]))
