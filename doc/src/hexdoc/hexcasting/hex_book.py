import logging
from pathlib import Path
from typing import Any, Mapping

from pydantic import Field, model_validator

from hexdoc.core.compat import HexVersion
from hexdoc.core.loader import ModResourceLoader
from hexdoc.core.metadata import HexdocMetadata
from hexdoc.core.properties import PatternStubProps
from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import I18n, Tag
from hexdoc.model import HexdocModel, init_context
from hexdoc.patchouli import Book, BookContext
from hexdoc.plugin import PluginManager
from hexdoc.utils.deserialize import cast_or_raise

from .pattern import Direction, PatternInfo


def load_hex_book(
    data: Mapping[str, Any],
    pm: PluginManager,
    loader: ModResourceLoader,
    i18n: I18n,
    all_metadata: dict[str, HexdocMetadata],
):
    with init_context(data):
        context = HexContext(
            pm=pm,
            loader=loader,
            i18n=i18n,
            # this SHOULD be set (as a ResourceLocation) by Book.get_book_json
            book_id=cast_or_raise(data["id"], ResourceLocation),
            all_metadata=all_metadata,
        )
    return Book.load_all_from_data(data, context)


class PatternMetadata(HexdocModel):
    """Automatically generated at `export_dir/modid.patterns.hexdoc.json`."""

    patterns: list[PatternInfo]

    @classmethod
    def path(cls, modid: str) -> Path:
        return Path(f"{modid}.patterns.hexdoc.json")


# conthext, perhaps
class HexContext(BookContext):
    patterns: dict[ResourceLocation, PatternInfo] = Field(default_factory=dict)

    @model_validator(mode="after")
    def _load_patterns(self):
        signatures = dict[str, PatternInfo]()  # just for duplicate checking

        match HexVersion.get():
            case HexVersion.v0_11_x:
                self._add_patterns_0_11(signatures)
            case HexVersion.v0_10_x | HexVersion.v0_9_x:
                self._add_patterns_0_10(signatures)

        # export patterns so addons can use them
        pattern_metadata = PatternMetadata(patterns=list(self.patterns.values()))
        self.loader.export(
            path=PatternMetadata.path(self.props.modid),
            data=pattern_metadata.model_dump_json(
                warnings=False,
                exclude_defaults=True,
            ),
        )

        # add external patterns AFTER exporting so we don't reexport them
        for metadata in self.loader.load_metadata(
            name_pattern="{modid}.patterns",
            model_type=PatternMetadata,
        ).values():
            for pattern in metadata.patterns:
                self._add_pattern(pattern, signatures)

        logging.getLogger(__name__).info(f"Loaded patterns: {self.patterns.keys()}")
        return self

    def _add_patterns_0_11(self, signatures: dict[str, PatternInfo]):
        # load the tag that specifies which patterns are random per world
        per_world = Tag.load(
            registry="action",
            id=ResourceLocation("hexcasting", "per_world_pattern"),
            context=self,
        )

        # for each stub, load all the patterns in the file
        for stub in self.props.pattern_stubs:
            for pattern in self._load_stub_patterns(stub, per_world):
                self._add_pattern(pattern, signatures)

    def _add_patterns_0_10(self, signatures: dict[str, PatternInfo]):
        for stub in self.props.pattern_stubs:
            for pattern in self._load_stub_patterns(stub, None):
                self._add_pattern(pattern, signatures)

    def _add_pattern(self, pattern: PatternInfo, signatures: dict[str, PatternInfo]):
        logging.getLogger(__name__).debug(f"Load pattern: {pattern.id}")

        # check for duplicates, because why not
        if duplicate := (
            self.patterns.get(pattern.id) or signatures.get(pattern.signature)
        ):
            raise ValueError(f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}")

        self.patterns[pattern.id] = pattern
        signatures[pattern.signature] = pattern

    def _load_stub_patterns(self, stub: PatternStubProps, per_world_tag: Tag | None):
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.
        logging.getLogger(__name__).info(f"Load pattern stub from {stub.path}")
        stub_text = stub.path.read_text("utf-8")

        for match in stub.regex.finditer(stub_text):
            groups = match.groupdict()
            id = self.props.mod_loc(groups["name"])

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
