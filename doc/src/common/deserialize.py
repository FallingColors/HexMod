import inspect
import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Callable, Type, TypeVar

import tomllib
from common.toml_placeholders import TOMLTable, fill_placeholders
from dacite import Config, DaciteError, UnionMatchError, from_dict
from dacite.dataclasses import get_fields


class Castable:
    """Abstract base class for types with a constructor in the form `C(value) -> C`.

    Subclassing this ABC allows for automatic deserialization using Dacite.
    """


TypeFn = Callable[[Any], Any]
TypeHooks = dict[Type[Any], TypeFn]


@dataclass
class TypedConfig(Config):
    """Dacite config, but with proper type hints and sane defaults."""

    type_hooks: TypeHooks = field(default_factory=dict)
    cast: list[TypeFn] = field(default_factory=list)
    check_types: bool = True
    strict: bool = True
    strict_unions_match: bool = True

    def __post_init__(self):
        self.cast.append(Castable)


def metadata(*, rename: str) -> dict[str, Any]:
    """Helper for specifying dataclass field metadata.

    Args:
        rename: The value under this key, if any, will instead be assigned to this field.
    """
    return {
        "rename": rename,
    }


def rename(rename: str) -> dict[str, Any]:
    """Helper for specifying field metadata to rename a FromPath field."""
    return metadata(rename=rename)


def handle_metadata_inplace(data_class: Type[Any], data: dict[str, Any]) -> None:
    """Applies our custom metadata. Currently this just renames fields."""
    for field in get_fields(data_class):
        try:
            key_name = field.metadata["rename"]
            if not isinstance(key_name, str):
                # TODO: raise?
                continue

            if field.name in data:
                # TODO: could instead keep a set of renamed fields, skip writing from a shadowed field
                raise ValueError(
                    f"Can't rename key '{key_name}' to field '{field.name}' because the key '{field.name}' also exists in the dict\n{data}"
                )
            data[field.name] = data.pop(key_name)
        except KeyError:
            pass


_T_json = TypeVar("_T_json", list[Any], dict[str, Any], str, int, bool, None)


def load_json(path: Path, _cls: Type[_T_json] = dict) -> _T_json:
    data: _T_json | Any = json.loads(path.read_text("utf-8"))
    if not isinstance(data, _cls):
        raise TypeError(f"Expected to load {_cls} from {path}, but got {type(data)}")
    return data


def load_json_data(
    data_class: Type[Any],
    path: Path,
    extra_data: dict[str, Any] = {},
) -> dict[str, Any]:
    """Load a dict from a JSON file and apply metadata transformations to it."""
    data = load_json(path)
    handle_metadata_inplace(data_class, data)
    data.update(extra_data)
    return data


def load_toml_data(data_class: Type[Any], path: Path) -> TOMLTable:
    data = tomllib.loads(path.read_text("utf-8"))
    fill_placeholders(data)
    handle_metadata_inplace(data_class, data)
    return data


_T = TypeVar("_T")


def from_dict_checked(
    data_class: Type[_T],
    data: dict[str, Any],
    config: TypedConfig,
    path: Path | None = None,
) -> _T:
    """Convert a dict to a dataclass.

    path is currently just used for error messages.
    """
    try:
        return from_dict(data_class, data, config)
    except Exception as e:
        if path:
            e.add_note(str(path))

        # horribly cursed workaround for https://github.com/konradhalas/dacite/issues/234
        # first, check if the message matches the one Dacite is raising
        if isinstance(e, KeyError) and e.args[0] == "popitem(): dictionary is empty":
            # local variables where the bad exception was raised
            f_locals = inspect.trace()[-1][0].f_locals
            data_ = f_locals["data"]
            union = f_locals["union"]

            # raise the actual correct exception
            new_e = UnionMatchError(union, data_)
            new_e.add_note(str(data_))
            raise new_e

        raise
