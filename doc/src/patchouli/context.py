from abc import ABC, abstractmethod
from pathlib import Path
from typing import Any, Generic, TypeVar, cast, dataclass_transform

from pydantic import ValidationInfo, model_validator

from common.model import AnyContext, HexDocModelFile
from common.properties import Properties
from minecraft.resource import ResourceLocation
from patchouli.formatting import FormatContext


class BookContext(FormatContext):
    props: Properties


AnyBookContext = TypeVar("AnyBookContext", bound=BookContext)


@dataclass_transform()
class BookModelFile(
    Generic[AnyContext, AnyBookContext],
    HexDocModelFile[AnyBookContext],
    ABC,
):
    id: ResourceLocation

    @classmethod
    @abstractmethod
    def _id_base_dir(cls, props: Properties) -> Path:
        ...

    @model_validator(mode="before")
    def _pre_root(cls, values: dict[str, Any], info: ValidationInfo) -> dict[str, Any]:
        if not info.context:
            return values

        context = cast(AnyBookContext, info.context)
        return values | {
            "id": ResourceLocation.from_file(
                modid=context["props"].modid,
                base_dir=cls._id_base_dir(context["props"]),
                path=values.pop("__path"),
            )
        }
