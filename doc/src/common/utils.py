from typing import Mapping, TypeVar

from common.abstract import Sortable

_T = TypeVar("_T")
_T_Sortable = TypeVar("_T_Sortable", bound=Sortable)


def sorted_dict(d: Mapping[_T, _T_Sortable]) -> dict[_T, _T_Sortable]:
    return dict(sorted(d.items(), key=lambda item: item[1]))
