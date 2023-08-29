import io
import json
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

from hexdoc.hexcasting.hex_book import load_hex_book
from hexdoc.minecraft.i18n import I18n
from hexdoc.patchouli.book import Book
from hexdoc.utils import Properties
from hexdoc.utils.cd import cd
from hexdoc.utils.model import HexdocModel
from hexdoc.utils.resource_loader import ModResourceLoader

from .jinja_extensions import IncludeRawExtension, hexdoc_block, hexdoc_wrap


def strip_empty_lines(text: str) -> str:
    return "\n".join(s for s in text.splitlines() if s.strip())


# CLI arguments
class Args(HexdocModel):
    """example: main.py properties.toml -o out.html"""

    properties_file: Path

    verbose: int
    ci: bool
    allow_missing: bool
    lang: str | None

    output_dir: Path | None
    export_only: bool
    list_langs: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = ArgumentParser()

        parser.add_argument("properties_file", type=Path)

        parser.add_argument("--verbose", "-v", action="count", default=0)
        parser.add_argument("--ci", action="store_true")
        parser.add_argument("--allow-missing", action="store_true")
        parser.add_argument("--lang", type=str, default=None)

        group = parser.add_mutually_exclusive_group(required=True)
        group.add_argument("--output_dir", "-o", type=Path)
        group.add_argument("--export-only", action="store_true")
        group.add_argument("--list-langs", action="store_true")

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
        assert bool(self.output_dir) + self.export_only + self.list_langs == 1

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

        props = Properties.load(args.properties_file)

        # just list the languages and exit
        if args.list_langs:
            with ModResourceLoader.load_all(props, export=False) as loader:
                langs = sorted(I18n.list_all(loader))
                print(json.dumps(langs))
                return

        # load everything
        with ModResourceLoader.clean_and_load_all(props) as loader:
            books = dict[str, Book]()

            if args.lang:
                first_lang = args.lang
                per_lang_i18n = {
                    first_lang: I18n.load(
                        loader,
                        lang=first_lang,
                        allow_missing=args.allow_missing,
                    )
                }
            else:
                first_lang = props.default_lang
                per_lang_i18n = I18n.load_all(
                    loader,
                    allow_missing=args.allow_missing,
                )

                # if export_only, skip actually loading the other languages' books
                if args.export_only:
                    per_lang_i18n = {first_lang: per_lang_i18n[first_lang]}

            _, book_data = Book.load_book_json(loader, props.book)

            # load one book with exporting enabled
            books[first_lang] = load_hex_book(
                book_data,
                loader,
                i18n=per_lang_i18n.pop(first_lang),
            )

            # then load the rest with exporting disabled for efficiency
            loader.export_dir = None
            for lang, i18n in per_lang_i18n.items():
                books[lang] = load_hex_book(book_data, loader, i18n)

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

        template = env.get_template(props.template.main)

        # set up the output directory
        subprocess.run(["git", "clean", "-fdX", args.output_dir])
        args.output_dir.mkdir(parents=True, exist_ok=True)

        static_dir = props.template.static_dir
        if static_dir and static_dir.is_dir():
            shutil.copytree(static_dir, args.output_dir, dirs_exist_ok=True)

        # render each language separately
        for lang, book in books.items():
            docs = strip_empty_lines(
                template.render(
                    **props.template.args,
                    book=book,
                    props=props,
                )
            )

            lang_output_dir = args.output_dir / lang
            lang_output_dir.mkdir(parents=True, exist_ok=True)
            (lang_output_dir / "index.html").write_text(docs, "utf-8")

            if lang == props.default_lang:
                (args.output_dir / "index.html").write_text(docs, "utf-8")


if __name__ == "__main__":
    main()
