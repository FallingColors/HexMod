from typing import TYPE_CHECKING, Any, Generic, TypeVar, dataclass_transform

from pydantic import BaseModel, ConfigDict, model_validator
from pydantic.config import ConfigDict
from typing_extensions import TypedDict

if TYPE_CHECKING:
    from pydantic.root_model import Model


AnyContext = TypeVar("AnyContext", bound=TypedDict)

DEFAULT_CONFIG = ConfigDict(
    extra="forbid",
)


@dataclass_transform()
class HexDocModel(Generic[AnyContext], BaseModel):
    model_config = DEFAULT_CONFIG

    # override the context type to use a generic TypedDict
    # TODO: open an issue on Pydantic for this
    if TYPE_CHECKING:

        @classmethod
        def model_validate(  # type: ignore
            cls: type[Model],
            obj: Any,
            *,
            strict: bool | None = None,
            from_attributes: bool | None = None,
            context: AnyContext | None = None,
        ) -> Model:
            ...

        @classmethod
        def model_validate_json(  # type: ignore
            cls: type[Model],
            json_data: str | bytes | bytearray,
            *,
            strict: bool | None = None,
            context: AnyContext | None = None,
        ) -> Model:
            ...


@dataclass_transform()
class HexDocStripHiddenModel(HexDocModel[AnyContext]):
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
