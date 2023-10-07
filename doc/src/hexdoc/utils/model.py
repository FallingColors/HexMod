from __future__ import annotations

from contextvars import ContextVar
from typing import TYPE_CHECKING, Any, Self, dataclass_transform

from pydantic import BaseModel, ConfigDict, model_validator
from pydantic.config import ConfigDict

from hexdoc.utils.contextmanagers import set_contextvar

DEFAULT_CONFIG = ConfigDict(
    extra="forbid",
)

_init_context_var = ContextVar[Any]("_init_context_var", default=None)


def init_context(value: Any):
    """https://docs.pydantic.dev/latest/usage/validators/#using-validation-context-with-basemodel-initialization"""
    return set_contextvar(_init_context_var, value)


@dataclass_transform()
class HexdocBaseModel(BaseModel):
    """Base class for all Pydantic models in hexdoc. You should probably use
    `HexdocModel` or `ValidationContext` instead.

    Sets the default model config, and overrides __init__ to allow using the
    `init_context` context manager to set validation context for constructors.
    """

    model_config = DEFAULT_CONFIG

    def __init__(__pydantic_self__, **data: Any) -> None:  # type: ignore
        __tracebackhide__ = True
        __pydantic_self__.__pydantic_validator__.validate_python(
            data,
            self_instance=__pydantic_self__,
            context=_init_context_var.get(),
        )

    __init__.__pydantic_base_init__ = True  # type: ignore


@dataclass_transform()
class ValidationContext(HexdocBaseModel):
    """Base class for Pydantic validation context for `HexdocModel`."""


@dataclass_transform()
class HexdocModel(HexdocBaseModel):
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
