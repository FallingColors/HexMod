from typing import Any, Generic, TypeVar

from common.model import AnyContext
from common.properties import Properties
from hexcasting.pattern import PatternInfo
from minecraft.resource import ResourceLocation
from patchouli.book import Book
from patchouli.context import AnyBookContext, BookContext


class HexContext(BookContext):
    patterns: dict[ResourceLocation, PatternInfo]


AnyHexContext = TypeVar("AnyHexContext", bound=HexContext)


class HexBookModel(
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
            for pattern in stub.load_patterns(props.modid, props.pattern_regex):
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


HexBook = HexBookModel[HexContext, HexContext, HexContext]
