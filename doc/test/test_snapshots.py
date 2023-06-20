import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterator

import pytest
from bs4 import BeautifulSoup as bs
from main import Args, main
from syrupy.assertion import SnapshotAssertion
from syrupy.extensions.amber import AmberSnapshotExtension
from syrupy.types import SerializedData


def prettify(data: SerializedData) -> str:
    return bs(data, features="html.parser").prettify()


class NoDiffSnapshotExtension(AmberSnapshotExtension):
    def diff_snapshots(
        self, serialized_data: SerializedData, snapshot_data: SerializedData
    ) -> SerializedData:
        return super().diff_snapshots(
            prettify(serialized_data), prettify(snapshot_data)
        )

    def diff_lines(
        self, serialized_data: SerializedData, snapshot_data: SerializedData
    ) -> Iterator[str]:
        return super().diff_lines(prettify(serialized_data), prettify(snapshot_data))


@dataclass
class DocgenArgs:
    out_path: Path
    snapshot: SnapshotAssertion
    argv: list[str]

    def assert_out_path(self):
        actual = self.out_path.read_text("utf-8")
        assert actual == self.snapshot


@pytest.fixture
def docgen(tmp_path: Path, snapshot: SnapshotAssertion) -> DocgenArgs:
    # arguments we want to pass to the docgen
    out_path = tmp_path / "out.html"
    return DocgenArgs(
        out_path,
        snapshot.use_extension(NoDiffSnapshotExtension),
        ["properties.toml", "-o", out_path.as_posix()],
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
    main(Args().parse_args(docgen.argv[:-2]))
    assert capsys.readouterr() == docgen.snapshot
