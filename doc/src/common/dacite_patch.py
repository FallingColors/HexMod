# pyright: reportPrivateUsage=false
# pyright: reportUnknownArgumentType=false
# pyright: reportUnknownMemberType=false

import copy
import traceback
from itertools import zip_longest
from typing import Any, Collection, Mapping, Type

import dacite.core
from dacite import Config, DaciteError, StrictUnionMatchError, UnionMatchError
from dacite.core import _build_value
from dacite.types import extract_generic, is_instance, is_optional, is_subclass


class UnionSkip(Exception):
    """Tagged union classes may raise this during initialization to say the data doesn't
    match their type."""


# fixes https://github.com/konradhalas/dacite/issues/234
# workaround for https://github.com/konradhalas/dacite/issues/218
# this code is, like, really bad. but to be fair dacite's isn't a whole lot better
# and as long as it works, does it really matter?
def _patched_build_value_for_union(union: Type[Any], data: Any, config: Config) -> Any:
    types = extract_generic(union)
    if is_optional(union) and len(types) == 2:
        return _build_value(type_=types[0], data=data, config=config)

    exceptions: list[Exception] = []
    union_matches = {}
    original_data = copy.deepcopy(data)
    data_ = data

    union_matches = {}
    for inner_type in types:
        try:
            try:
                value = _build_value(type_=inner_type, data=data, config=config)
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

    for e in exceptions:
        traceback.print_exception(e, limit=4)
        print("------------------------")

    e = UnionMatchError(field_type=union, value=data)
    e.add_note(f"\noriginal data: {original_data}")
    e.add_note(f"maybe-or-maybe-not-transformed data: {data}")
    e.add_note(f"transformed data: {data_}\n")
    raise e


# fixes https://github.com/konradhalas/dacite/issues/217
def _patched_build_value_for_collection(
    collection: Type[Any], data: Any, config: Config
) -> Any:
    data_type = data.__class__
    if isinstance(data, Mapping) and is_subclass(collection, Mapping):
        key_type, item_type = extract_generic(collection, defaults=(Any, Any))
        return data_type(
            (
                _build_value(type_=key_type, data=key, config=config),
                _build_value(type_=item_type, data=value, config=config),
            )
            for key, value in data.items()
        )
    elif isinstance(data, tuple) and is_subclass(collection, tuple):
        if not data:
            return data_type()
        types = extract_generic(collection)
        if len(types) == 2 and types[1] == Ellipsis:
            return data_type(
                _build_value(type_=types[0], data=item, config=config) for item in data
            )
        return data_type(
            _build_value(type_=type_, data=item, config=config)
            for item, type_ in zip_longest(data, types)
        )
    elif isinstance(data, Collection) and is_subclass(collection, Collection):
        item_type = extract_generic(collection, defaults=(Any,))[0]
        return data_type(
            _build_value(type_=item_type, data=item, config=config) for item in data
        )
    return data


# we do a bit of monkeypatching
dacite.core._build_value_for_union = _patched_build_value_for_union
dacite.core._build_value_for_collection = _patched_build_value_for_collection
