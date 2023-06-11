from typing import Any, Mapping, Protocol, TypeVar


class Comparable(Protocol):
    def __lt__(self, other: Any) -> bool:
        ...


_T = TypeVar("_T")
_T_Comparable = TypeVar("_T_Comparable", bound=Comparable)


def sorted_dict(d: Mapping[_T, _T_Comparable]) -> dict[_T, _T_Comparable]:
    return dict(sorted(d.items(), key=lambda item: item[1]))
