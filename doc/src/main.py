from pathlib import Path
from sys import stdout

from collate_data import parse_book, write_docs
from tap import Tap


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
        self.add_argument("output_file", nargs="?")


def main(args: Args) -> None:
    book = parse_book(args.root.as_posix(), args.mod_name, args.book_name)

    with args.template_file.open("r", encoding="utf-8") as template_f:
        if args.output_file:
            with args.output_file.open("w", encoding="utf-8") as output_f:
                write_docs(book, template_f, output_f)
        else:
            write_docs(book, template_f, stdout)


# entry point - just read the CLI args and run our main function with them
if __name__ == "__main__":
    main(Args().parse_args())
