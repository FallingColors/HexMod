from dataclasses import dataclass

import patchouli
from common.pattern import PatternInfo
from minecraft.resource import ResourceLocation


@dataclass
class HexBook(patchouli.Book):
    """Main docgen dataclass."""

    def __post_init_pre_categories__(self) -> None:
        self.blacklist: set[str] = set()
        self.spoilers: set[str] = set()

        # patterns
        self.patterns: dict[ResourceLocation, PatternInfo] = {}
        for stub in self.props.pattern_stubs:
            for pattern in stub.load_patterns(self.props.modid, self.props.pattern_re):
                # check for key clobbering, because why not
                if duplicate := self.patterns.get(pattern.id):
                    raise ValueError(
                        f"Duplicate pattern {pattern.id}\n{pattern}\n{duplicate}"
                    )
                self.patterns[pattern.id] = pattern
