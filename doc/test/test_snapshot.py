from pathlib import Path

from collate_data import main
from syrupy.assertion import SnapshotAssertion


def test_full_html(snapshot: SnapshotAssertion, tmp_path: Path):
    # generate output docs html file and assert it hasn't changed vs. the snapshot
    # arrange
    out_path = tmp_path / "out.html"
    argv = [
        "collate_data.py",
        "../Common/src/main/resources",  # resources dir
        "hexcasting",  # mod name
        "thehexbook",  # book name
        "template.html",  # template file
        out_path.as_posix(),  # output file
    ]

    # act
    main(argv)

    # assert
    actual = out_path.read_text()
    assert actual == snapshot
