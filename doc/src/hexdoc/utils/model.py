import logging
from pathlib import Path
from typing import TYPE_CHECKING, Any, Generic, Self, TypeVar, dataclass_transform

from pydantic import BaseModel, ConfigDict
from pydantic.config import ConfigDict
from typing_extensions import TypedDict

from .deserialize import load_json_dict

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
class HexDocFileModel(HexDocModel[AnyContext]):
    @classmethod
    def load(cls, path: Path, context: AnyContext) -> Self:
        logging.getLogger(__name__).debug(f"Load {cls}\n  path: {path}")
        data = load_json_dict(path) | {"__path": path}
        try:
            return cls.model_validate(data, context=context)
        except Exception as e:
            e.add_note(f"File: {path}")
            raise
