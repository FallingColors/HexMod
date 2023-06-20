# pyright: reportPrivateUsage=false
# pyright: reportUnknownArgumentType=false
# pyright: reportUnknownMemberType=false

import copy
import traceback
from typing import Any, Type

import dacite.core
from common.tagged_union import UnionSkip
from dacite import Config, DaciteError, StrictUnionMatchError, UnionMatchError
from dacite.core import _build_value
from dacite.types import extract_generic, is_instance, is_optional, transform_value


# fixes https://github.com/konradhalas/dacite/issues/234
# workaround for https://github.com/konradhalas/dacite/issues/218
# this code is, like, really bad. but to be fair dacite's isn't a whole lot better
# and as long as it works, does it really matter?
def _fixed_build_value_for_union(union: Type[Any], data: Any, config: Config) -> Any:
    types = extract_generic(union)
    if is_optional(union) and len(types) == 2:
        return _build_value(type_=types[0], data=data, config=config)

    exceptions: list[Exception] = []
    union_matches = {}
    original_data = copy.deepcopy(data)
    data_ = data
    for inner_type in types:
        try:
            try:
                data_ = transform_value(
                    type_hooks=config.type_hooks,
                    cast=config.cast,
                    target_type=inner_type,
                    value=data,
                )
            except Exception as e:
                if not isinstance(e, UnionSkip):
                    e.add_note(f"inner_type: {inner_type}")
                    exceptions.append(e)
                continue
            value = _build_value(type_=inner_type, data=data_, config=config)
            if is_instance(value, inner_type):
                if config.strict_unions_match:
                    union_matches[inner_type] = value
                else:
                    return value
        except DaciteError as e:
            e.add_note(f"inner_type: {inner_type}")
            exceptions.append(e)
            pass
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


# yup
dacite.core._build_value_for_union = _fixed_build_value_for_union
