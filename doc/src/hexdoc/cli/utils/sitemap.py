import shutil
from collections import defaultdict
from pathlib import Path

from pydantic import Field, TypeAdapter

from hexdoc.model import DEFAULT_CONFIG, HexdocModel
from hexdoc.utils.path import write_to_path

MARKER_NAME = ".sitemap-marker.json"


class SitemapMarker(HexdocModel):
    version: str
    lang: str
    path: str
    is_default_lang: bool

    @classmethod
    def load(cls, path: Path):
        return cls.model_validate_json(path.read_text("utf-8"))


class SitemapItem(HexdocModel):
    default_path: str = Field(alias="defaultPath", default="")
    lang_paths: dict[str, str] = Field(alias="langPaths", default_factory=dict)

    def add_marker(self, marker: SitemapMarker):
        self.lang_paths[marker.lang] = marker.path
        if marker.is_default_lang:
            self.default_path = marker.path


Sitemap = dict[str, SitemapItem]


def load_sitemap(root: Path) -> Sitemap:
    sitemap: Sitemap = defaultdict(SitemapItem)

    # crawl the new tree to rebuild the sitemap
    for marker_path in root.rglob(MARKER_NAME):
        marker = SitemapMarker.load(marker_path)
        sitemap[marker.version].add_marker(marker)

    return sitemap


def dump_sitemap(root: Path, sitemap: Sitemap):
    # dump the sitemap using a TypeAdapter so it serializes the items properly
    ta = TypeAdapter(Sitemap, config=DEFAULT_CONFIG)

    write_to_path(
        root / "meta" / "sitemap.json",
        ta.dump_json(sitemap, by_alias=True),
    )


def assert_version_exists(*, root: Path, version: str):
    # ensure the directory was written and it contains files (not just directories)
    path = root / "v" / version
    if not path.exists() or not any(path.rglob("*.*")):
        raise FileNotFoundError(f"Missing default language for {version}: {path}")


def delete_root_book(*, root: Path):
    """Remove the book from the site root."""
    for path in root.iterdir():
        if path.name in ["v", "meta"]:
            continue

        if path.is_dir():
            shutil.rmtree(path)
        else:
            path.unlink()


def delete_updated_books(*, src: Path, dst: Path):
    src_markers = src.rglob(MARKER_NAME)
    for marker in src_markers:
        src_dir = marker.parent
        dst_dir = dst / src_dir.relative_to(src)
        shutil.rmtree(dst_dir, ignore_errors=True)
