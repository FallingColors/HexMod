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

    source: Path
    dest: Path
    release: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser(allow_abbrev=False)

        parser.add_argument("--source", type=Path, required=True)
        parser.add_argument("--dest", type=Path, required=True)
        parser.add_argument("--release", default=False)

        return cls.model_validate(vars(parser.parse_args(args)))


def main():
    args = Args.parse_args()

    if not (args.source / "latest" / "index.html").is_file():
        raise FileNotFoundError(args.source / "index.html")

    args.dest.mkdir(parents=True, exist_ok=True)

    if args.release:
        # remove current latest-released book in the destination
        for path in args.dest.iterdir():
            if path.name not in ["v", "meta"]:
                shutil.rmtree(path)

    new_sitemap = defaultdict[str, dict[str, str]]()
    for marker_path in args.source.rglob(MARKER_NAME):
        # add new(?) version to the sitemap
        marker = SitemapMarker.model_validate_json(marker_path.read_text("utf-8"))
        new_sitemap[marker.version][marker.lang] = marker.path

        # delete the corresponding directory in the destination
        shutil.rmtree(args.dest / marker_path.relative_to(args.source))

    sitemap_path = args.dest / "meta" / "sitemap.json"

    if sitemap_path.is_file():
        sitemap = json.loads(sitemap_path.read_text("utf-8")) | new_sitemap
    else:
        sitemap = new_sitemap

    shutil.copytree(args.source, args.dest, dirs_exist_ok=True)
    write_to_path(sitemap_path, json.dumps(sitemap))


if __name__ == "__main__":
    main()
