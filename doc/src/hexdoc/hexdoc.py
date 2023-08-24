import io
import logging
import os
import sys
from argparse import ArgumentParser
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Self, Sequence

from jinja2 import ChoiceLoader, FileSystemLoader, PackageLoader, StrictUndefined
from jinja2.sandbox import SandboxedEnvironment

from hexdoc.hexcasting.hex_book import HexContext
from hexdoc.minecraft.i18n import I18n
from hexdoc.patchouli.book import Book
from hexdoc.utils import Properties
from hexdoc.utils.cd import cd
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.resource_loader import ModResourceLoader

from .jinja_extensions import IncludeRawExtension, hexdoc_block, hexdoc_wrap


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())


# CLI arguments
@dataclass
class Args:
    """example: main.py properties.toml -o out.html"""

    properties_file: Path
    output_file: Path | None
    verbose: int
    ci: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser()

        parser.add_argument("properties_file", type=Path)
        parser.add_argument("--output_file", "-o", type=Path)
        parser.add_argument("--verbose", "-v", action="count", default=0)
        parser.add_argument("--ci", action="store_true")

        return cls(**vars(parser.parse_args(args)))

    def __post_init__(self):
        # make paths absolute because we're cd'ing later
        self.properties_file = self.properties_file.resolve()
        if self.output_file:
            self.output_file = self.output_file.resolve()

        if self.ci and os.getenv("RUNNER_DEBUG") == "1":
            self.verbose = True

    @property
    def log_level(self) -> int:
        match self.verbose:
            case 0:
                return logging.WARNING
            case 1:
                return logging.INFO
            case _:
                return logging.DEBUG


def main(args: Args | None = None) -> None:
    # allow passing Args for test cases, but parse by default
    if args is None:
        args = Args.parse_args()

    # treat all paths as relative to the location of the properties file by cd-ing there
    with cd(args.properties_file.parent):
        # set stdout to utf-8 so printing to pipe or redirect doesn't break on Windows
        # (common windows L)
        assert isinstance(sys.stdout, io.TextIOWrapper)
        sys.stdout.reconfigure(encoding="utf-8")

        # set up logging
        logging.basicConfig(
            style="{",
            format="\033[1m[{relativeCreated:.02f} | {levelname} | {name}]\033[0m {message}",
            level=args.log_level,
        )

        # load the book
        props = Properties.load(args.properties_file)
        with ModResourceLoader.load_all(props) as loader:
            _, book_data = Book.load_book_json(loader, props.book)
            book = Book.load_all(
                book_data,
                HexContext(
                    props=props,
                    loader=loader,
                    i18n=I18n(
                        props=props,
                        loader=loader,
                        enabled=cast_or_raise(book_data["i18n"], bool),
                    ),
                    macros=cast_or_raise(book_data["macros"], dict[Any, Any]),
                ),
            )

            # set up Jinja environment
            env = SandboxedEnvironment(
                # search order: template_dirs, template_packages, built-in hexdoc templates
                loader=ChoiceLoader(
                    [FileSystemLoader(props.template_dirs)]
                    + [
                        PackageLoader(name, str(path))
                        for name, path in props.template_packages
                    ]
                ),
                undefined=StrictUndefined,
                lstrip_blocks=True,
                trim_blocks=True,
                autoescape=True,
                extensions=[
                    IncludeRawExtension,
                ],
            )
            env.filters |= {  # type: ignore
                "hexdoc_block": hexdoc_block,
                "hexdoc_wrap": hexdoc_wrap,
            }

            # load and render template
            template = env.get_template(props.template)
            docs = strip_empty_lines(
                template.render(
                    **props.template_args,
                    book=book,
                    props=props,
                    mod_metadata=loader.mod_metadata,
                )
            )

        # if there's an output file specified, write to it
        # otherwise just print the generated docs
        if args.output_file:
            args.output_file.write_text(docs, "utf-8")
        else:
            print(docs)


if __name__ == "__main__":
    main()
