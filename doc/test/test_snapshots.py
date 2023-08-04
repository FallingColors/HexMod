import subprocess
import sys
from pathlib import Path

import pytest
from syrupy.assertion import SnapshotAssertion

from hexdoc.hexdoc import Args, main

_RUN = ["hexdoc"]
_ARGV = ["properties.toml", "-o"]


def test_file(tmp_path: Path, snapshot: SnapshotAssertion):
    # generate output docs html file and assert it hasn't changed vs. the snapshot
    out_path = tmp_path / "out.html"
    main(Args().parse_args(_ARGV + [out_path.as_posix()]))
    assert out_path.read_text("utf-8") == snapshot


def test_cmd(tmp_path: Path, snapshot: SnapshotAssertion):
    # as above, but running the command we actually want to be using
    out_path = tmp_path / "out.html"
    subprocess.run(
        _RUN + _ARGV + [out_path.as_posix()],
        stdout=sys.stdout,
        stderr=sys.stderr,
    )
    assert out_path.read_text("utf-8") == snapshot


def test_stdout(capsys: pytest.CaptureFixture[str], snapshot: SnapshotAssertion):
    main(Args().parse_args(["properties.toml"]))
    assert capsys.readouterr() == snapshot
