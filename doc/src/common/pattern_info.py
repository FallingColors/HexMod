import re
from dataclasses import dataclass
from pathlib import Path


@dataclass
class RawPatternInfo:
    direction: str
    angle_sig: str
    is_per_world: bool


@dataclass
class PatternInfo(RawPatternInfo):
    modid: str
    name: str

    @property
    def resource_location(self) -> str:
        return f"{self.modid}:{self.name}"


_DEFAULT_PATTERN_RE = re.compile(
    r'HexPattern\.fromAngles\("([qweasd]+)", HexDir\.(\w+)\),\s*modLoc\("([^"]+)"\)([^;]*true\);)?'
)


@dataclass
class PatternStubFile:
    loader: str | None
    raw_path: str

    def path(self, resource_dir: Path) -> Path:
        file = resource_dir.parent / "java" / self.raw_path
        if self.loader is not None:
            file = Path(str(file).replace("Common", self.loader))
        return file

    def load_patterns(
        self,
        resource_dir: Path,
        modid: str,
        pattern_re: re.Pattern[str] = _DEFAULT_PATTERN_RE,
    ) -> dict[str, PatternInfo]:
        # TODO: add Gradle task to generate json with this data. this is dumb and fragile.

        patterns: dict[str, PatternInfo] = {}

        pattern_data = self.path(resource_dir).read_text("utf-8")
        for match in pattern_re.finditer(pattern_data):
            angle_sig, direction, name, is_per_world = match.groups()
            pattern = PatternInfo(
                direction,
                angle_sig,
                bool(is_per_world),
                modid,
                name,
            )
            patterns[pattern.resource_location] = pattern

        return patterns


def load_all_patterns(
    pattern_stubs: list[PatternStubFile],
    resource_dir: Path,
    modid: str,
    pattern_re: re.Pattern[str] = _DEFAULT_PATTERN_RE,
) -> dict[str, PatternInfo]:
    """Returns map from resource location (eg. hexcasting:brainsweep) to PatternInfo."""

    patterns: dict[str, PatternInfo] = {}
    for stub in pattern_stubs:
        patterns.update(stub.load_patterns(resource_dir, modid, pattern_re))
    return patterns
