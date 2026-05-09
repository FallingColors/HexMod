import logging
import re
from abc import ABC, abstractmethod
from pathlib import Path
from typing import Any, Iterable, Literal

from hexdoc.core import IsVersion, ModResourceLoader, Properties, ResourceLocation
from hexdoc.minecraft import Tag
from hexdoc.model import HexdocModel, StripHiddenModel, ValidationContextModel
from hexdoc.utils import TRACE, RelativePath
from pydantic import Field, TypeAdapter
from typing_extensions import override

from .utils.pattern import Direction, PatternInfo

logger = logging.getLogger(__name__)


class PatternMetadata(HexdocModel):
    """Automatically generated at `export_dir/modid.patterns.hexdoc.json`."""

    patterns: list[PatternInfo]

    @classmethod
    def path(cls, modid: str) -> Path:
        return Path(f"{modid}.patterns.hexdoc.json")


class BasePatternStubProps(StripHiddenModel, ABC):
    type: Any
    path: RelativePath
    required: bool = True
    """If `True` (the default), raise an error if no patterns were loaded from here."""

    def load_patterns(
        self,
        props: Properties,
        per_world_tag: Tag | None,
    ) -> list[PatternInfo]:
        logger.debug(f"Load {self.type} pattern stub from {self.path}")

        patterns = list[PatternInfo]()

        try:
            for pattern in self._iter_patterns(props):
                if per_world_tag is not None:
                    pattern.is_per_world = pattern.id in per_world_tag.values
                patterns.append(pattern)
        except Exception as e:
            # hack: notes don't seem to be working on pydantic exceptions :/
            logger.error(f"Failed to load {self.type} pattern stub from {self.path}.")
            raise e

        pretty_path = self.path.resolve().relative_to(Path.cwd())

        if self.required and not patterns:
            raise ValueError(self._no_patterns_error.format(path=pretty_path))

        logger.info(f"Loaded {len(patterns)} patterns from {pretty_path}")
        return patterns

    @abstractmethod
    def _iter_patterns(self, props: Properties) -> Iterable[PatternInfo]:
        """Loads and iterates over the patterns from this stub.

        Note: the `is_per_world` value returned by this function should be **ignored**
        in 0.11+, since that information can be found in the per world tag.
        """

    @property
    def _no_patterns_error(self) -> str:
        return "No patterns found in {path}, but required is True"


class RegexPatternStubProps(BasePatternStubProps):
    """Fetches pattern info by scraping source code with regex."""

    type: Literal["regex"] = "regex"
    regex: re.Pattern[str]
    per_world_value: str | None = "true"

    @override
    def _iter_patterns(self, props: Properties) -> Iterable[PatternInfo]:
        stub_text = self.path.read_text("utf-8")

        for match in self.regex.finditer(stub_text):
            groups = match.groupdict()

            if ":" in groups["name"]:
                id = ResourceLocation.from_str(groups["name"])
            else:
                id = props.mod_loc(groups["name"])

            yield PatternInfo(
                id=id,
                startdir=Direction[groups["startdir"]],
                signature=groups["signature"],
                is_per_world=groups.get("is_per_world") == self.per_world_value,
            )

    @property
    @override
    def _no_patterns_error(self):
        return super()._no_patterns_error + " (check the pattern regex)"


class JsonPatternStubProps(BasePatternStubProps):
    """Fetches pattern info from a JSON file."""

    type: Literal["json"]

    @override
    def _iter_patterns(self, props: Properties) -> Iterable[PatternInfo]:
        data = self.path.read_bytes()
        return TypeAdapter(list[PatternInfo]).validate_json(data)


PatternStubProps = RegexPatternStubProps | JsonPatternStubProps


class HexProperties(StripHiddenModel):
    pattern_stubs: list[PatternStubProps] = Field(default_factory=list)
    allow_duplicates: bool = False


# conthext, perhaps
class HexContext(ValidationContextModel):
    hex_props: HexProperties
    patterns: dict[ResourceLocation, PatternInfo] = Field(default_factory=dict)

    def load_patterns(self, loader: ModResourceLoader):
        signatures = dict[str, PatternInfo]()  # just for duplicate checking

        if IsVersion(">=1.20"):
            self._add_patterns_0_11(signatures, loader)
        else:
            self._add_patterns_0_10(signatures, loader.props)

        # export patterns so addons can use them
        pattern_metadata = PatternMetadata(patterns=list(self.patterns.values()))
        loader.export(
            path=PatternMetadata.path(loader.props.modid),
            data=pattern_metadata.model_dump_json(
                warnings=False,
                exclude_defaults=True,
            ),
        )

        # add external patterns AFTER exporting so we don't reexport them
        for metadata in loader.load_metadata(
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
        loader: ModResourceLoader,
    ):
        # load the tag that specifies which patterns are random per world
        per_world = Tag.load(
            registry="action",
            id=ResourceLocation("hexcasting", "per_world_pattern"),
            loader=loader,
        )

        # for each stub, load all the patterns in the file
        for stub in self.hex_props.pattern_stubs:
            for pattern in stub.load_patterns(loader.props, per_world):
                self._add_pattern(pattern, signatures)

    def _add_patterns_0_10(
        self,
        signatures: dict[str, PatternInfo],
        props: Properties,
    ):
        for stub in self.hex_props.pattern_stubs:
            for pattern in stub.load_patterns(props, None):
                self._add_pattern(pattern, signatures)

    def _add_pattern(self, pattern: PatternInfo, signatures: dict[str, PatternInfo]):
        logger.log(TRACE, f"Load pattern: {pattern.id}")

        # check for duplicates, because why not
        if duplicate := (
            self.patterns.get(pattern.id) or signatures.get(pattern.signature)
        ):
            message = f"pattern {pattern.id}\n{pattern}\n{duplicate}"
            if self.hex_props.allow_duplicates:
                logger.warning("Ignoring duplicate " + message)
                return
            raise ValueError("Duplicate" + message)

        self.patterns[pattern.id] = pattern
        signatures[pattern.signature] = pattern
