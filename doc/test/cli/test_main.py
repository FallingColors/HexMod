# pyright: reportUnknownMemberType=false

import subprocess
from pathlib import Path

import pytest
from pytest import FixtureRequest, TempPathFactory
from syrupy.assertion import SnapshotAssertion

from hexdoc.cli.main import render

PROPS_FILE = Path("doc/properties.toml")

RENDERED_FILENAMES = [
    "v/latest/index.html",
    "v/latest/index.css",
    "v/latest/textures.css",
    "v/latest/index.js",
]


@pytest.fixture(scope="class")
def output_dir(request: FixtureRequest, tmp_path_factory: TempPathFactory) -> Path:
    if isinstance(request.cls, type):
        basename = request.cls.__name__
    else:
        basename = "tmp"
    return tmp_path_factory.mktemp(basename)


@pytest.fixture(scope="class", params=RENDERED_FILENAMES)
def rendered_file(request: FixtureRequest, output_dir: Path) -> Path:
    return output_dir / request.param


class TestApp:
    def test_render(self, output_dir: Path):
        render(
            props_file=PROPS_FILE,
            output_dir=output_dir,
            lang="en_us",
        )

    def test_file(self, rendered_file: Path, path_snapshot: SnapshotAssertion):
        assert rendered_file == path_snapshot


class TestSubprocess:
    def test_render(self, output_dir: Path):
        cmd = [
            "hexdoc",
            "render",
            PROPS_FILE.as_posix(),
            output_dir.as_posix(),
            "--lang=en_us",
        ]
        subprocess.run(cmd)

    def test_file(self, rendered_file: Path, path_snapshot: SnapshotAssertion):
        assert rendered_file == path_snapshot
