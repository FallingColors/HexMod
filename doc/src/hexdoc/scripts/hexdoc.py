# because Tap.add_argument isn't typed, for some reason
# pyright: reportUnknownMemberType=false

from pathlib import Path

from jinja2 import (
    ChoiceLoader,
    Environment,
    FileSystemLoader,
    PackageLoader,
    StrictUndefined,
)

# from jinja2.sandbox import SandboxedEnvironment
from tap import Tap

from hexdoc.hexcasting import HexBook
from hexdoc.properties import Properties
from hexdoc.utils.jinja_extensions import IncludeRawExtension, hexdoc_block, hexdoc_wrap


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())


# CLI arguments
class Args(Tap):
    """example: main.py properties.toml -o out.html"""

    properties_file: Path
    output_file: Path | None

    def configure(self):
        self.add_argument("properties_file")
        self.add_argument("-o", "--output_file", required=False)


def main(args: Args | None = None) -> None:
    # allow passing Args for test cases, but parse by default
    if args is None:
        args = Args().parse_args()

    # load the properties and book
    props = Properties.load(args.properties_file)
    book = HexBook.load(*HexBook.prepare(props))

    # set up Jinja environment
    # TODO: SandboxedEnvironment
    env = Environment(
        # search order: template_dirs, template_packages, built-in hexdoc templates
        loader=ChoiceLoader(
            [FileSystemLoader(props.template_dirs)]
            + [PackageLoader(name, str(path)) for name, path in props.template_packages]
            + [PackageLoader("hexdoc", "_templates")]
        ),
        undefined=StrictUndefined,
        lstrip_blocks=True,
        trim_blocks=True,
        autoescape=True,
        extensions=[IncludeRawExtension],
    )
    env.filters |= dict(  # for some reason, pylance doesn't like the {} here
        hexdoc_block=hexdoc_block,
        hexdoc_wrap=hexdoc_wrap,
    )

    # load and render template
    template = env.get_template(props.template)
    docs = strip_empty_lines(
        template.render(
            **props.template_args,
            book=book,
            props=props,
        )
    )

    # if there's an output file specified, write to it
    # otherwise just print the generated docs
    if args.output_file:
        args.output_file.write_text(docs, "utf-8")
    else:
        print(docs)


# entry point: just read the CLI args and pass them to the actual logic
if __name__ == "__main__":
    main()
