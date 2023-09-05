import shutil
from argparse import ArgumentParser
from collections import defaultdict
from pathlib import Path
from typing import Self, Sequence

from pydantic import Field, TypeAdapter

from hexdoc.utils import DEFAULT_CONFIG, HexdocModel
from hexdoc.utils.path import write_to_path

from .hexdoc import MARKER_NAME, SitemapMarker


class SitemapItem(HexdocModel):
    default_path: str = Field(alias="defaultPath", default="")
    lang_paths: dict[str, str] = Field(alias="langPaths", default_factory=dict)

    def add_marker(self, marker: SitemapMarker):
        self.lang_paths[marker.lang] = marker.path
        if marker.is_default_lang:
            self.default_path = marker.path


Sitemap = dict[str, SitemapItem]


# CLI arguments
class Args(HexdocModel):
    """example: main.py properties.toml -o out.html"""

    src: Path
    dst: Path
    is_release: bool
    update_latest: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser(allow_abbrev=False)

        parser.add_argument("--src", type=Path, required=True)
        parser.add_argument("--dst", type=Path, required=True)
        parser.add_argument("--is-release", default=False)
        parser.add_argument("--update-latest", default=True)

        return cls.model_validate(vars(parser.parse_args(args)))


def assert_version_exists(src: Path, version: str):
    path = src / "v" / version / "index.html"
    if not path.is_file():
        raise FileNotFoundError(f"Missing default language for {version}: {path}")


def main():
    args = Args.parse_args()

    # ensure at least the default language was built successfully
    if args.update_latest:
        assert_version_exists(args.src, "latest")
    # TODO: figure out how to do this with pluggy
    # if args.is_release:
    #     assert_version_exists(args.src, GRADLE_VERSION)

    args.dst.mkdir(parents=True, exist_ok=True)

    # remove the book from the root of the destination since we're adding a new one now
    if args.is_release and args.update_latest:
        for path in args.dst.iterdir():
            if path.name in ["v", "meta"]:
                continue

            if path.is_dir():
                shutil.rmtree(path)
            else:
                path.unlink()

    # find all the marked directories in source and delete them from dest
    for marker_path in args.src.rglob(MARKER_NAME):
        dst_marker_dir = args.dst / marker_path.parent.relative_to(args.src)
        shutil.rmtree(dst_marker_dir, ignore_errors=True)

    # that should be all the possible conflicts, so copy src into dst now
    shutil.copytree(args.src, args.dst, dirs_exist_ok=True)

    # crawl the new tree to rebuild the sitemap
    sitemap: Sitemap = defaultdict(SitemapItem)
    for marker_path in args.dst.rglob(MARKER_NAME):
        marker = SitemapMarker.load(marker_path)
        sitemap[marker.version].add_marker(marker)

    # dump the sitemap using a TypeAdapter so it serializes the items properly
    ta = TypeAdapter(Sitemap, config=DEFAULT_CONFIG)
    write_to_path(
        args.dst / "meta" / "sitemap.json",
        ta.dump_json(sitemap, by_alias=True),
    )


if __name__ == "__main__":
    main()
