import io
import logging
import os
import shutil
import subprocess
import sys
from argparse import ArgumentParser
from pathlib import Path
from typing import Self, Sequence

from jinja2 import ChoiceLoader, FileSystemLoader, PackageLoader, StrictUndefined
from jinja2.sandbox import SandboxedEnvironment
from pydantic import field_validator, model_validator

from hexdoc.hexcasting.hex_book import HexContext
from hexdoc.patchouli.book import Book
from hexdoc.utils import Properties
from hexdoc.utils.cd import cd
from hexdoc.utils.model import HexdocModel, init_context
from hexdoc.utils.resource_loader import ModResourceLoader

from .jinja_extensions import IncludeRawExtension, hexdoc_block, hexdoc_wrap


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())


# CLI arguments
class Args(HexdocModel):
    """example: main.py properties.toml -o out.html"""

    properties_file: Path
    output_dir: Path | None
    export_only: bool
    verbose: int
    ci: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser()

        parser.add_argument("properties_file", type=Path)
        parser.add_argument("--verbose", "-v", action="count", default=0)
        parser.add_argument("--ci", action="store_true")

        group = parser.add_mutually_exclusive_group(required=True)
        group.add_argument("--output_dir", "-o", type=Path)
        group.add_argument("--export-only", "-e", action="store_true")

        return cls.model_validate(vars(parser.parse_args(args)))

    @field_validator("properties_file", "output_dir", mode="after")
    def _resolve_path(cls, value: Path | None):
        # make paths absolute because we're cd'ing later
        match value:
            case Path():
                return value.resolve()
            case _:
                return value

    @model_validator(mode="after")
    def _post_root(self):
        if self.ci and os.getenv("RUNNER_DEBUG") == "1":
            self.verbose = True

        # exactly one of these must be truthy (should be enforced by group above)
        assert bool(self.output_dir) != self.export_only

        return self

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
        with ModResourceLoader.clean_and_load_all(props) as loader:
            _, book_data = Book.load_book_json(loader, props.book)

            with init_context(book_data):
                context = HexContext(loader=loader)

            book = Book.model_validate(book_data, context=context)

        if args.export_only:
            return
        assert args.output_dir

        # set up Jinja environment
        env = SandboxedEnvironment(
            # search order: template_dirs, template_packages
            loader=ChoiceLoader(
                [FileSystemLoader(props.template.dirs)]
                + [
                    PackageLoader(name, str(path))
                    for name, path in props.template.packages
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
        template = env.get_template(props.template.main)
        docs = strip_empty_lines(
            template.render(
                **props.template.args,
                book=book,
                props=props,
            )
        )

        # write docs
        subprocess.run(["git", "clean", "-fdX", args.output_dir])
        args.output_dir.mkdir(parents=True, exist_ok=True)

        static_dir = props.template.static_dir
        if static_dir and static_dir.is_dir():
            shutil.copytree(static_dir, args.output_dir, dirs_exist_ok=True)

        (args.output_dir / "index.html").write_text(docs, "utf-8")


if __name__ == "__main__":
    main()
