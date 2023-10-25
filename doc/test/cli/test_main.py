# pyright: reportUnknownMemberType=false

import subprocess
from pathlib import Path

import pytest
from pytest import TempPathFactory
from syrupy.assertion import SnapshotAssertion

from hexdoc.cli.main import render

PROPS_FILE = Path("doc/properties.toml")

RENDERED_FILENAMES = [
    "v/latest/index.html",
    "v/latest/index.css",
    "v/latest/textures.css",
    "v/latest/index.js",
]


@pytest.fixture(scope="session")
def app_output_dir(tmp_path_factory: TempPathFactory) -> Path:
    return tmp_path_factory.mktemp("app", numbered=False)


@pytest.fixture(scope="session")
def subprocess_output_dir(tmp_path_factory: TempPathFactory) -> Path:
    return tmp_path_factory.mktemp("subprocess", numbered=False)


def test_render_app(app_output_dir: Path):
    render(
        props_file=PROPS_FILE,
        output_dir=app_output_dir,
        lang="en_us",
    )


def test_render_subprocess(subprocess_output_dir: Path):
    cmd = [
        "hexdoc",
        "render",
        PROPS_FILE.as_posix(),
        subprocess_output_dir.as_posix(),
        "--lang=en_us",
    ]
    subprocess.run(cmd)


@pytest.mark.parametrize("filename", RENDERED_FILENAMES)
def test_files(
    filename: str,
    app_output_dir: Path,
    subprocess_output_dir: Path,
    path_snapshot: SnapshotAssertion,
):
    app_file = app_output_dir / filename
    subprocess_file = subprocess_output_dir / filename

    assert app_file.read_bytes() == subprocess_file.read_bytes()
    assert app_file == path_snapshot
