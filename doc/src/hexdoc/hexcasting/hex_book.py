from pathlib import Path
from typing import Any, Generic, TypeVar

from hexdoc.patchouli import AnyBookContext, Book, BookContext
from hexdoc.properties import Properties
from hexdoc.resource import ResourceLocation
from hexdoc.utils import AnyContext

from .pattern import Direction, PatternInfo


class HexContext(BookContext):
    patterns: dict[ResourceLocation, PatternInfo]


AnyHexContext = TypeVar("AnyHexContext", bound=HexContext)


class HexBookType(
    Generic[AnyContext, AnyBookContext, AnyHexContext],
    Book[AnyHexContext, AnyHexContext],
):
    @classmethod
    def prepare(cls, props: Properties) -> tuple[dict[str, Any], HexContext]:
        data, context = super().prepare(props)

        # load patterns
        patterns = dict[ResourceLocation, PatternInfo]()
        signatures = dict[str, PatternInfo]()  # just for duplicate checking
        for stub in props.pattern_stubs:
            # for each stub, load all the patterns in the file
            for pattern in cls.load_patterns(stub, props):
                # check for duplicates, because why not
                if duplicate := (
                    patterns.get(pattern.id) or signatures.get(pattern.signature)
                ):
                    raise ValueError(
                        f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}"
                    )
                patterns[pattern.id] = pattern
                signatures[pattern.signature] = pattern

        # build new context
        return data, {
            **context,
            "patterns": patterns,
        }

    @classmethod
    def load_patterns(cls, path: Path, props: Properties):
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.
        stub_text = path.read_text("utf-8")
        for match in props.pattern_regex.finditer(stub_text):
            signature, startdir, name, is_per_world = match.groups()
            yield PatternInfo(
                startdir=Direction[startdir],
                signature=signature,
                is_per_world=bool(is_per_world),
                id=ResourceLocation(props.modid, name),
            )


# type alias for convenience
HexBook = HexBookType[HexContext, HexContext, HexContext]
