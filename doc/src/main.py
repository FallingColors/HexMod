# pyright: reportUnknownMemberType=false

import sys
from pathlib import Path

from collate_data import generate_docs
from common.pattern_info import PatternStubFile
from patchouli.book import Book
from tap import Tap

if sys.version_info < (3, 11):
    raise RuntimeError("Minimum Python version: 3.11")

_PATTERN_STUBS = [
    PatternStubFile(None, "at/petrak/hexcasting/interop/pehkui/PehkuiInterop.java"),
    PatternStubFile(None, "at/petrak/hexcasting/common/casting/RegisterPatterns.java"),
    PatternStubFile(
        "Fabric",
        "at/petrak/hexcasting/fabric/interop/gravity/GravityApiInterop.java",
    ),
]

# CLI arguments
class Args(Tap):
    """example: main.py ../Common/src/main/resources hexcasting thehexbook template.html out.html"""

    resources_dir: Path
    mod_name: str
    book_name: str
    template_file: Path
    output_file: Path | None

    @property
    def root(self) -> Path:
        return self.resources_dir

    def configure(self):
        # set all arguments as positional
        self.add_argument("resources_dir")
        self.add_argument("mod_name")
        self.add_argument("book_name")
        self.add_argument("template_file")
        self.add_argument("output_file", help="(Path, optional)", nargs="?")


def main(args: Args) -> None:
    # read the book and template, then fill the template
    book = Book(args.root, args.mod_name, args.book_name, _PATTERN_STUBS)
    template = args.template_file.read_text("utf-8")

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
