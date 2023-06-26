# make sure we patch dacite before doing any parsing
# should this be a PR? probably! TODO: i'll do it later
from common import dacite_patch as _  # isort: skip

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Callable, Type, TypeVar

import tomllib
from dacite import Config, from_dict

from common.dacite_patch import handle_metadata
from common.toml_placeholders import TOMLDict, fill_placeholders
from common.types import Castable, JSONDict, JSONValue, isinstance_or_raise

_T_Input = TypeVar("_T_Input")

_T_Dataclass = TypeVar("_T_Dataclass")

TypeHook = Callable[[_T_Dataclass | Any], _T_Dataclass | dict[str, Any]]

TypeHooks = dict[Type[_T_Dataclass], TypeHook[_T_Dataclass]]

TypeHookMaker = Callable[[_T_Input], TypeHooks[_T_Dataclass]]


@dataclass
class TypedConfig(Config):
    """Dacite config, but with proper type hints and sane defaults."""

    type_hooks: TypeHooks[Any] = field(default_factory=dict)
    cast: list[TypeHook[Any]] = field(default_factory=list)
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


def load_json_object(path: Path) -> JSONDict:
    data: JSONValue = json.loads(path.read_text("utf-8"))
    assert isinstance_or_raise(data, dict)
    return data


def load_json_data(
    data_class: Type[Any],
    path: Path,
    extra_data: dict[str, Any] = {},
) -> dict[str, Any]:
    """Load a dict from a JSON file and apply metadata transformations to it."""
    data = load_json_object(path)
    return handle_metadata(data_class, data) | extra_data


def load_toml_data(data_class: Type[Any], path: Path) -> TOMLDict:
    data = tomllib.loads(path.read_text("utf-8"))
    fill_placeholders(data)
    return handle_metadata(data_class, data)


def from_dict_checked(
    data_class: Type[_T_Dataclass],
    data: dict[str, Any],
    config: TypedConfig,
    path: Path | None = None,
) -> _T_Dataclass:
    """Convert a dict to a dataclass.

    path is currently just used for error messages.
    """
    try:
        return from_dict(data_class, data, config)
    except Exception as e:
        if path:
            e.add_note(str(path))
        raise
