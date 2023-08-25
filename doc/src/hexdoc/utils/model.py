from __future__ import annotations

from typing import TYPE_CHECKING, Any, Self, dataclass_transform

from pydantic import BaseModel, ConfigDict, model_validator
from pydantic.config import ConfigDict

DEFAULT_CONFIG = ConfigDict(
    extra="forbid",
)


@dataclass_transform()
class ValidationContext(BaseModel):
    """Base class for Pydantic validation context for `HexDocModel`."""

    model_config = DEFAULT_CONFIG


@dataclass_transform()
class HexDocModel(BaseModel):
    """Base class for most Pydantic models in hexdoc.

    Includes type overrides to require using subclasses of `ValidationContext` for
    validation context.
    """

    model_config = DEFAULT_CONFIG

    # pydantic core actually allows PyAny for context, so I'm pretty sure this is fine
    if TYPE_CHECKING:

        @classmethod
        def model_validate(  # pyright: ignore[reportIncompatibleMethodOverride]
            cls,
            obj: Any,
            *,
            strict: bool | None = None,
            from_attributes: bool | None = None,
            context: ValidationContext | None = None,
        ) -> Self:
            ...

        @classmethod
        def model_validate_json(  # pyright: ignore[reportIncompatibleMethodOverride]
            cls,
            json_data: str | bytes | bytearray,
            *,
            strict: bool | None = None,
            context: ValidationContext | None = None,
        ) -> Self:
            ...


@dataclass_transform()
class StripHiddenModel(HexDocModel):
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
