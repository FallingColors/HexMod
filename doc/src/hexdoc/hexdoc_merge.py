import json
import shutil
from argparse import ArgumentParser
from collections import defaultdict
from pathlib import Path
from typing import Self, Sequence

from hexdoc.hexdoc import MARKER_NAME, SitemapMarker
from hexdoc.utils import HexdocModel
from hexdoc.utils.path import write_to_path


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())


# CLI arguments
class Args(HexdocModel):
    """example: main.py properties.toml -o out.html"""

    src: Path
    dst: Path
    release: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser(allow_abbrev=False)

        parser.add_argument("--src", type=Path, required=True)
        parser.add_argument("--dst", type=Path, required=True)
        parser.add_argument("--release", default=False)

        return cls.model_validate(vars(parser.parse_args(args)))


def main():
    args = Args.parse_args()

    # ensure at least the default language was built successfully
    latest_default = args.src / "v" / "latest" / "index.html"
    if not latest_default.is_file():
        raise FileNotFoundError(latest_default)

    args.dst.mkdir(parents=True, exist_ok=True)

    # remove the book from the root of the destination since we're adding a new one now
    if args.release:
        for path in args.dst.iterdir():
            if path.name not in ["v", "meta"]:
                shutil.rmtree(path)

    # find all the marked directories in source and delete them from dest
    for marker_path in args.src.rglob(MARKER_NAME):
        dst_marker_dir = args.dst / marker_path.parent.relative_to(args.src)
        shutil.rmtree(dst_marker_dir, ignore_errors=True)

    # that should be all the possible conflicts, so merge src into dst now
    shutil.move(args.src / "*", args.dst)

    # crawl the new tree to rebuild the sitemap
    sitemap = defaultdict[str, dict[str, str]](dict)

    for marker_path in args.dst.rglob(MARKER_NAME):
        marker = SitemapMarker.load(marker_path)
        sitemap[marker.version][marker.lang] = marker.path

    write_to_path(args.dst / "meta" / "sitemap.json", json.dumps(sitemap))


if __name__ == "__main__":
    main()
