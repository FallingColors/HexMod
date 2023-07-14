# because Tap.add_argument isn't typed, for some reason
# pyright: reportUnknownMemberType=false

import sys
from pathlib import Path

from jinja2 import Environment, FileSystemLoader, StrictUndefined

# from jinja2.sandbox import SandboxedEnvironment
from tap import Tap

from common.properties import Properties
from common.templates import IncludeRawExtension, hexdoc_block, hexdoc_minify
from hexcasting.hex_book import HexBook

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
    book = HexBook.load(*HexBook.prepare(props))

    # set up Jinja environment
    # TODO: SandboxedEnvironment
    env = Environment(
        # TODO: ChoiceLoader w/ ModuleLoader, but we need the directory restructure
        loader=FileSystemLoader("./templates"),
        undefined=StrictUndefined,
        lstrip_blocks=True,
        trim_blocks=True,
        autoescape=False,
        extensions=[IncludeRawExtension],
    )
    env.filters["hexdoc_minify"] = hexdoc_minify
    env.filters["hexdoc_block"] = hexdoc_block

    # load and render template
    template = env.get_template(props.template)
    docs = template.render(
        props.template_args
        | {
            "book": book,
            "spoilers": props.spoilers,
            "blacklist": props.blacklist,
        }
    )

    # if there's an output file specified, write to it
    # otherwise just print the generated docs
    if args.output_file:
        args.output_file.write_text(docs, "utf-8")
    else:
        print(docs)


# entry point: just read the CLI args and pass them to the actual logic
if __name__ == "__main__":
    main(Args().parse_args())
