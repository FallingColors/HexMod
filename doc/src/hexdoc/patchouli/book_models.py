import logging
from abc import ABC
from pathlib import Path
from typing import Generic, Self, TypeVar, dataclass_transform

from hexdoc.utils import AnyContext, ResourceLocation
from hexdoc.utils.deserialize import load_json_dict
from hexdoc.utils.model import HexDocModel

from .text.formatting import FormatContext


class BookContext(FormatContext):
    pass


AnyBookContext = TypeVar("AnyBookContext", bound=BookContext)


@dataclass_transform()
class BookFileModel(
    Generic[AnyContext, AnyBookContext],
    HexDocModel[AnyBookContext],
    ABC,
):
    id: ResourceLocation

    @classmethod
    def load(cls, id: ResourceLocation, path: Path, context: AnyBookContext) -> Self:
        logging.getLogger(__name__).debug(f"Load {cls}\n  path: {path}")

        try:
            data = load_json_dict(path) | {"id": id}
            return cls.model_validate(data, context=context)
        except Exception as e:
            e.add_note(f"File: {path}")
            raise
