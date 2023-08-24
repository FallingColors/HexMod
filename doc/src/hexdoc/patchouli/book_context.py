from typing import Self

from pydantic import Field, model_validator

from hexdoc.minecraft import Tag
from hexdoc.utils import ResourceLocation

from .text.formatting import FormattingContext


class BookContext(FormattingContext):
    spoilered_advancements: set[ResourceLocation] = Field(default_factory=set)

    @model_validator(mode="after")
    def _post_root_load_tags(self) -> Self:
        self.spoilered_advancements |= Tag.load(
            registry="hexdoc",
            id=ResourceLocation("hexcasting", "spoilered_advancements"),
            context=self,
        ).values

        return self
