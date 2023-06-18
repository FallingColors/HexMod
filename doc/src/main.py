# because Tap.add_argument isn't typed, for some reason
# pyright: reportUnknownMemberType=false

import sys
from pathlib import Path

from collate_data import generate_docs
from common.properties import Properties
from patchouli.book import Book
from tap import Tap

if sys.version_info < (3, 11):
    raise RuntimeError("Minimum Python version: 3.11")

# CLI arguments
class Args(Tap):
    """example: main.py properties.toml -o out.html"""

    properties_file: Path
    output_file: Path | None

    def configure(self):
        # set as positional
        self.add_argument("properties_file")


def main(args: Args) -> None:
    # load the properties and book
    properties = Properties.load(args.properties_file)
    book = Book(properties)

    # load and fill the template
    template = properties.template.read_text("utf-8")
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
