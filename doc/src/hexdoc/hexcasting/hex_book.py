import logging

from hexdoc.patchouli.book import BookContext
from hexdoc.utils import Properties, ResourceLocation
from hexdoc.utils.properties import PatternStubProps

from .pattern import Direction, PatternInfo


# conthext, perhaps
class HexContext(BookContext):
    patterns: dict[ResourceLocation, PatternInfo]


def load_patterns(props: Properties):
    patterns = dict[ResourceLocation, PatternInfo]()
    signatures = dict[str, PatternInfo]()  # just for duplicate checking

    for stub in props.pattern_stubs:
        # for each stub, load all the patterns in the file
        for pattern in _load_stub_patterns(stub, props):
            logging.getLogger(__name__).debug(f"Load pattern: {pattern.id}")

            # check for duplicates, because why not
            if duplicate := (
                patterns.get(pattern.id) or signatures.get(pattern.signature)
            ):
                raise ValueError(
                    f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}"
                )

            patterns[pattern.id] = pattern
            signatures[pattern.signature] = pattern

    return patterns


def _load_stub_patterns(stub: PatternStubProps, props: Properties):
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
