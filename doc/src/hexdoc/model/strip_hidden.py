from typing import Any, dataclass_transform

from pydantic import model_validator

from hexdoc.model import HexdocModel


@dataclass_transform()
class StripHiddenModel(HexdocModel):
    """Base model which removes all keys starting with _ before validation."""

    @model_validator(mode="before")
    def _pre_root_strip_hidden(cls, values: Any) -> Any:
        if not isinstance(values, dict):
            return values

        return {
            key: value
            for key, value in values.items()
            if not (isinstance(key, str) and key.startswith("_"))
        }
