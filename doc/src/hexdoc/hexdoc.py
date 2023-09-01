import io
import json
import logging
import os
import shutil
import sys
from argparse import ArgumentParser
from pathlib import Path
from typing import Self, Sequence

from jinja2 import ChoiceLoader, FileSystemLoader, PackageLoader, StrictUndefined
from jinja2.sandbox import SandboxedEnvironment
from pydantic import field_validator, model_validator

from hexdoc.hexcasting.hex_book import load_hex_book
from hexdoc.minecraft import I18n
from hexdoc.patchouli import Book
from hexdoc.utils import HexdocModel, ModResourceLoader, Properties
from hexdoc.utils.cd import cd
from hexdoc.utils.path import write_to_path

from .__version__ import GRADLE_VERSION
from .utils.jinja_extensions import IncludeRawExtension, hexdoc_block, hexdoc_wrap

MARKER_NAME = ".sitemap-marker.json"


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
    is_release: bool
    clean: bool

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
        parser.add_argument("--clean", action="store_true")
        # do this instead of store_true because it's easier to use with Actions
        parser.add_argument("--is-release", default=False)

        group = parser.add_mutually_exclusive_group(required=True)
        group.add_argument("--output-dir", "-o", type=Path)
        group.add_argument("--export-only", action="store_true")
        group.add_argument("--list-langs", action="store_true")

        return cls.model_validate(vars(parser.parse_args(args)))

    @field_validator(
        "properties_file",
        "output_dir",
        mode="after",
    )
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


class SitemapMarker(HexdocModel):
    version: str
    lang: str
    path: str
    is_default_lang: bool

    @classmethod
    def load(cls, path: Path):
        return cls.model_validate_json(path.read_text("utf-8"))


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
        logger = logging.getLogger(__name__)

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
        if args.clean:
            shutil.rmtree(args.output_dir, ignore_errors=True)

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

        static_dir = props.template.static_dir

        versions = ["latest"]
        if args.is_release:
            # root should be the latest released version
            versions += ["", GRADLE_VERSION]

        # render each version and language separately
        for version in versions:
            for lang, book in books.items():
                is_default_lang = lang == props.default_lang

                # /index.html
                # /lang/index.html
                # /v/version/index.html
                # /v/version/lang/index.html
                parts = ("v", version) if version else ()
                if not is_default_lang:
                    parts += (lang,)

                output_dir = args.output_dir / Path(*parts)
                page_url = "/".join((props.url,) + parts)

                logger.info(f"Rendering {output_dir}")
                docs = strip_empty_lines(
                    template.render(
                        **props.template.args,
                        book=book,
                        props=props,
                        page_url=page_url,
                        version=version or GRADLE_VERSION,
                        lang=lang,
                        is_bleeding_edge=version == "latest",
                    )
                )

                write_to_path(output_dir / "index.html", docs)
                if static_dir:
                    shutil.copytree(static_dir, output_dir, dirs_exist_ok=True)

                # marker file for updating the sitemap later
                # we use this because matrix doesn't have outputs
                # this feels scuffed but it does work
                if version:
                    marker = SitemapMarker(
                        version=version,
                        lang=lang,
                        path="/" + "/".join(parts),
                        is_default_lang=is_default_lang,
                    )
                    (output_dir / MARKER_NAME).write_text(marker.model_dump_json())


if __name__ == "__main__":
    main()
