import logging
import re
from pathlib import Path

from hexdoc.core import IsVersion, Properties, ResourceLocation
from hexdoc.minecraft import Tag
from hexdoc.model import HexdocModel, StripHiddenModel, ValidationContext
from hexdoc.patchouli import BookContext
from hexdoc.utils import TRACE, RelativePath
from pydantic import Field

from .utils.pattern import Direction, PatternInfo

logger = logging.getLogger(__name__)


class PatternMetadata(HexdocModel):
    """Automatically generated at `export_dir/modid.patterns.hexdoc.json`."""

    patterns: list[PatternInfo]

    @classmethod
    def path(cls, modid: str) -> Path:
        return Path(f"{modid}.patterns.hexdoc.json")


class PatternStubProps(StripHiddenModel):
    path: RelativePath
    regex: re.Pattern[str]
    per_world_value: str | None = "true"


class HexProperties(StripHiddenModel):
    pattern_stubs: list[PatternStubProps]


# conthext, perhaps
class HexContext(ValidationContext):
    hex_props: HexProperties
    patterns: dict[ResourceLocation, PatternInfo] = Field(default_factory=dict)

    def load_patterns(self, context: BookContext):
        signatures = dict[str, PatternInfo]()  # just for duplicate checking

        if IsVersion(">=1.20"):
            self._add_patterns_0_11(signatures, context)
        else:
            self._add_patterns_0_10(signatures, context.props)

        # export patterns so addons can use them
        pattern_metadata = PatternMetadata(patterns=list(self.patterns.values()))
        context.loader.export(
            path=PatternMetadata.path(context.props.modid),
            data=pattern_metadata.model_dump_json(
                warnings=False,
                exclude_defaults=True,
            ),
        )

        # add external patterns AFTER exporting so we don't reexport them
        for metadata in context.loader.load_metadata(
            name_pattern="{modid}.patterns",
            model_type=PatternMetadata,
            allow_missing=True,
        ).values():
            for pattern in metadata.patterns:
                self._add_pattern(pattern, signatures)

        logger.debug(f"Loaded patterns: {self.patterns.keys()}")
        return self

    def _add_patterns_0_11(
        self,
        signatures: dict[str, PatternInfo],
        context: BookContext,
    ):
        # load the tag that specifies which patterns are random per world
        per_world = Tag.load(
            registry="action",
            id=ResourceLocation("hexcasting", "per_world_pattern"),
            loader=context.loader,
        )

        # for each stub, load all the patterns in the file
        for stub in self.hex_props.pattern_stubs:
            for pattern in self._load_stub_patterns(context.props, stub, per_world):
                self._add_pattern(pattern, signatures)

    def _add_patterns_0_10(
        self,
        signatures: dict[str, PatternInfo],
        props: Properties,
    ):
        for stub in self.hex_props.pattern_stubs:
            for pattern in self._load_stub_patterns(props, stub, None):
                self._add_pattern(pattern, signatures)

    def _add_pattern(self, pattern: PatternInfo, signatures: dict[str, PatternInfo]):
        logger.log(TRACE, f"Load pattern: {pattern.id}")

        # check for duplicates, because why not
        if duplicate := (
            self.patterns.get(pattern.id) or signatures.get(pattern.signature)
        ):
            raise ValueError(f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}")

        self.patterns[pattern.id] = pattern
        signatures[pattern.signature] = pattern

    def _load_stub_patterns(
        self,
        props: Properties,
        stub: PatternStubProps,
        per_world_tag: Tag | None,
    ):
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.
        logger.debug(f"Load pattern stub from {stub.path}")
        stub_text = stub.path.read_text("utf-8")

        for match in stub.regex.finditer(stub_text):
            groups = match.groupdict()
            id = props.mod_loc(groups["name"])

            if per_world_tag is not None:
                is_per_world = id in per_world_tag.values
            else:
                is_per_world = groups.get("is_per_world") == stub.per_world_value

            yield PatternInfo(
                id=id,
                startdir=Direction[groups["startdir"]],
                signature=groups["signature"],
                is_per_world=is_per_world,
            )
