import logging
from argparse import ArgumentParser
from dataclasses import dataclass
from pathlib import Path
from typing import Self, Sequence

from jinja2 import (
    ChoiceLoader,
    Environment,
    FileSystemLoader,
    PackageLoader,
    StrictUndefined,
)

from hexdoc.hexcasting import HexBook
from hexdoc.utils import Properties

from .jinja_extensions import IncludeRawExtension, hexdoc_block, hexdoc_wrap

# TODO: enable
# from jinja2.sandbox import SandboxedEnvironment


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())


# CLI arguments
@dataclass
class Args:
    """example: main.py properties.toml -o out.html"""

    properties_file: Path
    output_file: Path | None
    verbose: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser()

        parser.add_argument("properties_file", type=Path)
        parser.add_argument("--output_file", "-o", type=Path)
        parser.add_argument("--verbose", "-v", action="store_true")

        return cls(**vars(parser.parse_args(args)))


def main(args: Args | None = None) -> None:
    # allow passing Args for test cases, but parse by default
    if args is None:
        args = Args.parse_args()

    logging.basicConfig(
        style="{",
        format="[{levelname}][{name}] {message}",
    )
    logger = logging.getLogger(__name__)

    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
        logger.debug("Log level set to DEBUG")

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
    env.filters |= dict(  # pyright: ignore[reportUnknownMemberType]
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
