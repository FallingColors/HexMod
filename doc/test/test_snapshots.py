import subprocess
import sys
from pathlib import Path

from syrupy.assertion import SnapshotAssertion

from hexdoc.hexdoc import Args, main

PROPS = "doc/properties.toml"


def test_file(tmp_path: Path, snapshot: SnapshotAssertion):
    # generate output docs html file and assert it hasn't changed vs. the snapshot
    main(
        Args.parse_args(
            [PROPS, "--lang", "en_us", "--release", "-o", tmp_path.as_posix()]
        )
    )
    out_path = tmp_path / "index.html"
    assert out_path.read_text("utf-8") == snapshot


def test_cmd(tmp_path: Path, snapshot: SnapshotAssertion):
    # as above, but running the command we actually want to be using
    subprocess.run(
        ["hexdoc", PROPS, "--lang", "en_us", "--release", "-o", tmp_path.as_posix()],
        stdout=sys.stdout,
        stderr=sys.stderr,
    )
    out_path = tmp_path / "index.html"
    assert out_path.read_text("utf-8") == snapshot
