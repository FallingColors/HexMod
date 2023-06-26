from dataclasses import dataclass
from typing import Any

from common.pattern import PatternInfo
from common.state import BookState
from minecraft.resource import ResourceLocation
from patchouli.book import Book


@dataclass
class HexBookState(BookState):
    def __post_init__(self, *args: Any, **kwargs: Any):
        super().__post_init__(*args, **kwargs)

        # mutable state
        self.blacklist: set[str] = set()
        self.spoilers: set[str] = set()

        # patterns
        self.patterns: dict[ResourceLocation, PatternInfo] = {}
        for stub in self.props.pattern_stubs:
            # for each stub, load all the patterns in the file
            for pattern in stub.load_patterns(self.props.modid, self.props.pattern_re):
                # check for key clobbering, because why not
                if duplicate := self.patterns.get(pattern.id):
                    raise ValueError(
                        f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}"
                    )
                self.patterns[pattern.id] = pattern
