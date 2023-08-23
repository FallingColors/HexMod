import logging
from typing import Mapping, Self

from pydantic import Field, model_validator

from hexdoc.minecraft.tags import Tag
from hexdoc.patchouli.book import BookContext
from hexdoc.utils import ResourceLocation
from hexdoc.utils.properties import PatternStubProps

from .pattern import Direction, PatternInfo


# conthext, perhaps
class HexContext(BookContext):
    patterns: Mapping[ResourceLocation, PatternInfo] = Field(default_factory=dict)

    @model_validator(mode="after")
    def _post_root_load_patterns(self) -> Self:
        # load the tag that specifies which patterns are random per world
        per_world = Tag.load(
            registry="action",
            id=ResourceLocation("hexcasting", "per_world_pattern"),
            context=self,
        )

        self.patterns = dict()
        signatures = dict[str, PatternInfo]()  # just for duplicate checking

        for stub in self.props.pattern_stubs:
            # for each stub, load all the patterns in the file
            for pattern in self._load_stub_patterns(stub, per_world):
                logging.getLogger(__name__).debug(f"Load pattern: {pattern.id}")

                # check for duplicates, because why not
                if duplicate := (
                    self.patterns.get(pattern.id) or signatures.get(pattern.signature)
                ):
                    raise ValueError(
                        f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}"
                    )

                self.patterns[pattern.id] = pattern
                signatures[pattern.signature] = pattern

        return self

    def _load_stub_patterns(self, stub: PatternStubProps, per_world: Tag):
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.
        stub_text = stub.path.read_text("utf-8")

        for match in stub.regex.finditer(stub_text):
            groups = match.groupdict()
            id = self.props.mod_loc(groups["name"])

            yield PatternInfo(
                startdir=Direction[groups["startdir"]],
                signature=groups["signature"],
                is_per_world=id in per_world.values,
                id=id,
            )
