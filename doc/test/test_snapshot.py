import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

import pytest
from main import Args, main
from syrupy.assertion import SnapshotAssertion


@dataclass
class DocgenArgs:
    out_path: Path
    snapshot: SnapshotAssertion
    argv: list[str]

    def assert_out_path(self):
        actual = self.out_path.read_text()
        assert actual == self.snapshot


@pytest.fixture
def docgen(tmp_path: Path, snapshot: SnapshotAssertion) -> DocgenArgs:
    # arguments we want to pass to the docgen
    out_path = tmp_path / "out.html"
    return DocgenArgs(
        out_path,
        snapshot,
        [
            "../Common/src/main/resources",
            "hexcasting",
            "thehexbook",
            "template.html",
            out_path.as_posix(),
        ],
    )


def test_file(docgen: DocgenArgs):
    # generate output docs html file and assert it hasn't changed vs. the snapshot
    main(Args().parse_args(docgen.argv))
    docgen.assert_out_path()


def test_cmd(docgen: DocgenArgs):
    # as above, but running the command we actually want to be using
    subprocess.run(
        [sys.executable, "src/main.py"] + docgen.argv,
        stdout=sys.stdout,
        stderr=sys.stderr,
    )
    docgen.assert_out_path()


def test_stdout(docgen: DocgenArgs, capsys: pytest.CaptureFixture[str]):
    main(Args().parse_args(docgen.argv[:-1]))
    assert capsys.readouterr() == docgen.snapshot
