# because Tap.add_argument isn't typed, for some reason
# pyright: reportUnknownMemberType=false

# make sure we patch dacite before doing any parsing
# this is also in common.deserialize but hey, it doesn't hurt to put it here too
# should this be a PR? probably! TODO: i'll do it later
from common import dacite_patch as _  # isort: skip

import sys
from pathlib import Path

from tap import Tap

from collate_data import generate_docs
from common.properties import Properties
from hexcasting.hex_state import HexBookState
from patchouli.book import Book

if sys.version_info < (3, 11):
    raise RuntimeError("Minimum Python version: 3.11")

# CLI arguments
class Args(Tap):
    """example: main.py properties.toml -o out.html"""

    properties_file: Path
    output_file: Path | None

    def configure(self):
        self.add_argument("properties_file")
        self.add_argument("-o", "--output_file", required=False)


def main(args: Args) -> None:
    # load the properties and book
    props = Properties.load(args.properties_file)
    book = Book.load(HexBookState(props))

    # load and fill the template
    template = props.template.read_text("utf-8")
    docs = generate_docs(book, template)

    # if there's an output file specified, write to it
    # otherwise just print the generated docs
    if args.output_file:
        args.output_file.write_text(docs, "utf-8")
    else:
        print(docs)


# entry point: just read the CLI args and pass them to the actual logic
if __name__ == "__main__":
    main(Args().parse_args())
