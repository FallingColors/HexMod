import logging
from typing import Any, Generic, TypeVar

from hexdoc.patchouli import AnyBookContext, Book, BookContext
from hexdoc.utils import AnyContext, Properties, ResourceLocation
from hexdoc.utils.properties import PatternStubProps

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

        logging.getLogger(__name__).debug(f"Patterns: {patterns.keys()}")

        # build new context
        return data, {
            **context,
            "patterns": patterns,
        }

    @classmethod
    def load_patterns(cls, stub: PatternStubProps, props: Properties):
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.
        stub_text = stub.path.read_text("utf-8")
        for match in stub.regex.finditer(stub_text):
            groups = match.groupdict()
            yield PatternInfo(
                startdir=Direction[groups["startdir"]],
                signature=groups["signature"],
                # is_per_world=bool(is_per_world), # FIXME: idfk how to do this now
                id=props.mod_loc(groups["name"]),
            )


# type alias for convenience
HexBook = HexBookType[HexContext, HexContext, HexContext]
