import subprocess
import sys
from dataclasses import Field, fields
from pathlib import Path
from typing import Any, Iterator

import pytest
from bs4 import BeautifulSoup as bs
from syrupy.assertion import SnapshotAssertion
from syrupy.extensions.amber import AmberSnapshotExtension
from syrupy.types import SerializedData

from common.formatting import FormatTree
from common.properties import Properties
from common.types import LocalizedStr
from hexcasting.hex_state import HexBookState
from main import Args, main
from patchouli.book import Book


def prettify(data: SerializedData) -> str:
    return bs(data, features="html.parser").prettify()


class NoDiffSnapshotEx(AmberSnapshotExtension):
    def diff_snapshots(
        self, serialized_data: SerializedData, snapshot_data: SerializedData
    ) -> SerializedData:
        return "no diff"

    def diff_lines(
        self, serialized_data: SerializedData, snapshot_data: SerializedData
    ) -> Iterator[str]:
        yield from ["no diff"]


_RUN = [
    sys.executable,
    "src/main.py",
]
_ARGV = ["properties.toml", "-o"]

longrun = pytest.mark.skipif("not config.getoption('longrun')")


def test_file(tmp_path: Path, snapshot: SnapshotAssertion):
    # generate output docs html file and assert it hasn't changed vs. the snapshot
    out_path = tmp_path / "out.html"
    main(Args().parse_args(_ARGV + [out_path.as_posix()]))
    assert out_path.read_text("utf-8") == snapshot.use_extension(NoDiffSnapshotEx)


@longrun
def test_file_pretty(tmp_path: Path, snapshot: SnapshotAssertion):
    # generate output docs html file and assert it hasn't changed vs. the snapshot
    out_path = tmp_path / "out.html"
    main(Args().parse_args(_ARGV + [out_path.as_posix()]))
    assert prettify(out_path.read_text("utf-8")) == snapshot


def test_cmd(tmp_path: Path, snapshot: SnapshotAssertion):
    # as above, but running the command we actually want to be using
    out_path = tmp_path / "out.html"
    subprocess.run(
        _RUN + _ARGV + [out_path.as_posix()],
        stdout=sys.stdout,
        stderr=sys.stderr,
    )
    assert out_path.read_text("utf-8") == snapshot.use_extension(NoDiffSnapshotEx)


def test_stdout(capsys: pytest.CaptureFixture[str], snapshot: SnapshotAssertion):
    main(Args().parse_args(["properties.toml"]))
    assert capsys.readouterr() == snapshot.use_extension(NoDiffSnapshotEx)


def test_book_text(snapshot: SnapshotAssertion):
    def test_field(data_class: Any, field: Field[Any]):
        value = getattr(data_class, field.name, None)
        if isinstance(value, (LocalizedStr, FormatTree)):
            assert value == snapshot

    props = Properties.load(Path("properties.toml"))
    book = Book.load(HexBookState(props))

    for field in fields(book):
        test_field(book, field)

    for category in book.categories.values():
        for field in fields(category):
            test_field(category, field)

        for entry in category.entries:
            for field in fields(entry):
                test_field(entry, field)

            for page in entry.pages:
                for field in fields(page):
                    test_field(page, field)
