# pyright: reportPrivateUsage=false
# pyright: reportUnknownArgumentType=false
# pyright: reportUnknownMemberType=false

import copy
from itertools import zip_longest
from typing import (
    Any,
    ClassVar,
    Collection,
    Mapping,
    Type,
    TypeVar,
    get_args,
    get_origin,
    get_type_hints,
)

import dacite.core
import dacite.types
from dacite import (
    Config,
    DaciteError,
    StrictUnionMatchError,
    UnionMatchError,
    from_dict as _original_from_dict,
)
from dacite.cache import cache
from dacite.core import _build_value as _original_build_value
from dacite.data import Data
from dacite.dataclasses import get_fields
from dacite.types import extract_generic, is_instance, is_optional, is_subclass

from common.types import isinstance_or_raise


class UnionSkip(Exception):
    """Tagged union classes may raise this during initialization to say the data doesn't
    match their type."""


def handle_metadata(data_class: Type[Any], data: dict[str, Any]):
    """Applies our custom metadata. Currently this just renames fields."""
    # only transform a dict once, in case this is called multiple times
    data = data.copy()
    if data.get("__metadata_handled"):  # mischief managed?
        return data
    data["__metadata_handled"] = True

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

    return data


def handle_metadata_final(data_class: Type[Any], data: dict[str, Any]):
    """As `handle_metadata`, but removes the key marking data as handled.

    Should only be used within a custom from_dict implementation.
    """
    data = handle_metadata(data_class, data)
    data.pop("__metadata_handled")
    return data


def _patched_build_value(type_: Type[Any], data: Any, config: Config) -> Any:
    if type_ not in config.type_hooks:
        origin = get_origin(type_)
        if origin and origin in config.type_hooks:
            data = config.type_hooks[origin](data)
    return _original_build_value(type_, data, config)


# fixes https://github.com/konradhalas/dacite/issues/234
# workaround for https://github.com/konradhalas/dacite/issues/218
# this code is, like, really bad. but to be fair dacite's isn't a whole lot better
# and as long as it works, does it really matter?
def _patched_build_value_for_union(union: Type[Any], data: Any, config: Config) -> Any:
    types = extract_generic(union)
    if is_optional(union) and len(types) == 2:
        return _patched_build_value(type_=types[0], data=data, config=config)

    exceptions: list[Exception] = []
    union_matches = {}
    original_data = copy.deepcopy(data)
    data_ = data

    union_matches = {}
    for inner_type in types:
        try:
            try:
                value = _patched_build_value(type_=inner_type, data=data, config=config)
            except UnionSkip:
                continue
            except Exception as e:
                e.add_note(f"inner_type: {inner_type}")
                exceptions.append(e)
                continue
            if is_instance(value, inner_type):
                if config.strict_unions_match:
                    union_matches[inner_type] = value
                else:
                    return value
        except DaciteError as e:
            e.add_note(f"inner_type: {inner_type}")
            exceptions.append(e)

    if config.strict_unions_match and union_matches:
        if len(union_matches) > 1:
            e = StrictUnionMatchError(union_matches)
            e.add_note(f"union_matches: {union_matches}")
            exceptions.append(e)
        else:
            return union_matches.popitem()[1]
    if not config.check_types:
        return data

    e = UnionMatchError(field_type=union, value=data)
    e.add_note(f"\noriginal data: {original_data}")
    e.add_note(f"maybe-or-maybe-not-transformed data: {data}")
    e.add_note(f"transformed data: {data_}\n")
    exceptions.append(e)

    raise ExceptionGroup("Failed to match union", exceptions)


# fixes https://github.com/konradhalas/dacite/issues/217
def _patched_build_value_for_collection(
    collection: Type[Any], data: Any, config: Config
) -> Any:
    data_type = data.__class__
    if isinstance(data, Mapping) and is_subclass(collection, Mapping):
        key_type, item_type = extract_generic(collection, defaults=(Any, Any))
        return data_type(
            (
                _patched_build_value(type_=key_type, data=key, config=config),
                _patched_build_value(type_=item_type, data=value, config=config),
            )
            for key, value in data.items()
        )
    elif isinstance(data, tuple) and is_subclass(collection, tuple):
        if not data:
            return data_type()
        types = extract_generic(collection)
        if len(types) == 2 and types[1] == Ellipsis:
            return data_type(
                _patched_build_value(type_=types[0], data=item, config=config)
                for item in data
            )
        return data_type(
            _patched_build_value(type_=type_, data=item, config=config)
            for item, type_ in zip_longest(data, types)
        )
    elif isinstance(data, Collection) and is_subclass(collection, Collection):
        item_type = extract_generic(collection, defaults=(Any,))[0]
        return data_type(
            _patched_build_value(type_=item_type, data=item, config=config)
            for item in data
        )
    return data


_T = TypeVar("_T")


def _patched_from_dict(
    data_class: Type[_T],
    data: Data,
    config: Config | None = None,
) -> _T:
    if isinstance(data, data_class):
        return data

    # ensure it's a dict, or add context
    try:
        assert isinstance_or_raise(data, dict)
    except TypeError as e:
        if config and data_class not in config.type_hooks:
            e.add_note(f"Note: {data_class} is not in type_hooks: {config.type_hooks}")
        else:
            e.add_note(f"data_class: {data_class}")
        raise

    data = handle_metadata_final(data_class, data)
    return _original_from_dict(data_class, data, config)


def _patched_is_valid_generic_class(value: Any, type_: Type[Any]) -> bool:
    origin = get_origin(type_)
    if not (origin and isinstance(value, origin)):
        return False
    type_args = get_args(type_)
    type_hints = cache(get_type_hints)(type(value))
    for field_name, field_type in type_hints.items():
        field_value = getattr(value, field_name, None)
        if isinstance(field_type, TypeVar):
            # TODO: this will fail to detect incorrect type in some cases
            # see comments on https://github.com/konradhalas/dacite/pull/209
            if not any(is_instance(field_value, arg) for arg in type_args):
                return False
        elif get_origin(field_type) is not ClassVar:
            if not is_instance(field_value, field_type):
                return False
    return True


# we do a bit of monkeypatching
dacite.from_dict = _patched_from_dict
dacite.core.from_dict = _patched_from_dict
dacite.core._build_value = _patched_build_value
dacite.core._build_value_for_union = _patched_build_value_for_union
dacite.core._build_value_for_collection = _patched_build_value_for_collection
dacite.types.is_valid_generic_class = _patched_is_valid_generic_class
