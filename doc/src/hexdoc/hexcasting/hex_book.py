import logging
from pathlib import Path
from typing import Any, Mapping

from pydantic import Field, model_validator

from hexdoc.minecraft import I18n, Tag
from hexdoc.patchouli import Book, BookContext
from hexdoc.plugin import PluginManager
from hexdoc.utils import HexdocModel, ModResourceLoader, ResourceLocation, init_context
from hexdoc.utils.compat import HexVersion
from hexdoc.utils.properties import PatternStubProps

from .pattern import Direction, PatternInfo


def load_hex_book(
    data: Mapping[str, Any],
    pm: PluginManager,
    loader: ModResourceLoader,
    i18n: I18n,
):
    with init_context(data):
        context = HexContext(pm=pm, loader=loader, i18n=i18n)
    return Book.model_validate(data, context=context)


class PatternMetadata(HexdocModel):
    """Automatically generated at `export_dir/modid.patterns.hexdoc.json`."""

    patterns: dict[ResourceLocation, PatternInfo]

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
            case HexVersion.v0_11:
                self._add_patterns_0_11(signatures)
            case HexVersion.v0_10 | HexVersion.v0_9:
                self._add_patterns_0_10(signatures)

        # export patterns so addons can use them
        pattern_metadata = PatternMetadata(
            patterns=self.patterns,
        )
        self.loader.export(
            path=PatternMetadata.path(self.props.modid),
            data=pattern_metadata.model_dump_json(warnings=False),
        )

        # add external patterns AFTER exporting so we don't reexport them
        for metadata in self.loader.load_metadata(
            "{modid}.patterns", PatternMetadata
        ).values():
            for _, pattern in metadata.patterns.items():
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

    def _load_stub_patterns(self, stub: PatternStubProps, per_world: Tag | None):
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.
        logging.getLogger(__name__).debug(f"Load pattern stub from {stub.path}")
        stub_text = stub.path.read_text("utf-8")

        for match in stub.regex.finditer(stub_text):
            groups = match.groupdict()
            id = self.props.mod_loc(groups["name"])

            yield PatternInfo(
                startdir=Direction[groups["startdir"]],
                signature=groups["signature"],
                is_per_world=(id in per_world.values)
                if per_world
                else groups.get("is_per_world") == "true",
                id=id,
            )
