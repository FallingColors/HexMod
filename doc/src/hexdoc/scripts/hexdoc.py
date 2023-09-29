# pyright: reportUnknownMemberType=false, reportUnknownArgumentType=false
# pyright: reportUnknownLambdaType=false

import io
import json
import logging
import os
import shutil
import sys
from argparse import ArgumentParser
from pathlib import Path
from typing import Self, Sequence

from jinja2 import (
    ChoiceLoader,
    FileSystemLoader,
    PackageLoader,
    StrictUndefined,
    Template,
)
from jinja2.sandbox import SandboxedEnvironment
from pydantic import model_validator

from hexdoc.hexcasting.hex_book import load_hex_book
from hexdoc.minecraft import I18n
from hexdoc.patchouli import Book
from hexdoc.plugin import PluginManager
from hexdoc.utils import HexdocModel, ModResourceLoader, Properties
from hexdoc.utils.deserialize import cast_or_raise
from hexdoc.utils.jinja_extensions import (
    IncludeRawExtension,
    hexdoc_block,
    hexdoc_localize,
    hexdoc_texture_url,
    hexdoc_wrap,
)
from hexdoc.utils.path import write_to_path
from hexdoc.utils.resource_loader import HexdocMetadata

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
    update_latest: bool
    clean: bool

    output_dir: Path | None
    export_only: bool
    list_langs: bool

    @classmethod
    def parse_args(cls, args: Sequence[str] | None = None) -> Self:
        parser = cls._parser()
        args_dict = vars(parser.parse_args(args))
        return cls.model_validate(args_dict)

    @classmethod
    def _parser(cls):
        parser = ArgumentParser()

        parser.add_argument("properties_file", type=Path)

        parser.add_argument("--verbose", "-v", action="count", default=0)
        parser.add_argument("--ci", action="store_true")
        parser.add_argument("--allow-missing", action="store_true")
        parser.add_argument("--lang", type=str, default=None)
        parser.add_argument("--clean", action="store_true")

        # do this instead of store_true because it's easier to use with Actions
        parser.add_argument("--is-release", default=False)
        parser.add_argument("--update-latest", default=True)

        group = parser.add_mutually_exclusive_group(required=True)
        group.add_argument("--output-dir", "-o", type=Path)
        group.add_argument("--export-only", action="store_true")
        group.add_argument("--list-langs", action="store_true")

        return parser

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
    # set stdout to utf-8 so printing to pipe or redirect doesn't break on Windows
    # (common windows L)
    cast_or_raise(sys.stdout, io.TextIOWrapper).reconfigure(encoding="utf-8")
    cast_or_raise(sys.stderr, io.TextIOWrapper).reconfigure(encoding="utf-8")

    # allow passing Args for test cases, but parse by default
    if args is None:
        args = Args.parse_args()

    # set up logging
    logging.basicConfig(
        style="{",
        format="\033[1m[{relativeCreated:.02f} | {levelname} | {name}]\033[0m {message}",
        level=args.log_level,
    )

    logger = logging.getLogger(__name__)
    logger.info("Starting.")

    # Properties is the main config file for hexdoc
    props = Properties.load(args.properties_file)
    logger.debug(props)

    # load plugins
    pm = PluginManager()
    version = pm.mod_version(props.modid)
    logger.info(f"Building docs for {props.modid} {version}")

    # just list the languages and exit
    if args.list_langs:
        with ModResourceLoader.load_all(props, pm, export=False) as loader:
            langs = sorted(I18n.list_all(loader))
            print(json.dumps(langs))
            return

    # load everything
    with ModResourceLoader.clean_and_load_all(props, pm) as loader:
        books = dict[str, tuple[Book, I18n]]()

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
        first_i18n = per_lang_i18n.pop(first_lang)
        books[first_lang] = (
            load_hex_book(book_data, pm, loader, first_i18n),
            first_i18n,
        )

        # then load the rest with exporting disabled for efficiency
        loader.export_dir = None
        for lang, i18n in per_lang_i18n.items():
            books[lang] = (load_hex_book(book_data, pm, loader, i18n), i18n)

        mod_metadata = loader.mod_metadata

    if args.export_only:
        return

    # set up Jinja

    env = SandboxedEnvironment(
        # search order: template_dirs, template_packages
        loader=ChoiceLoader(
            [FileSystemLoader(props.template.dirs)]
            + [PackageLoader(name, str(path)) for name, path in props.template.packages]
        ),
        undefined=StrictUndefined,
        lstrip_blocks=True,
        trim_blocks=True,
        autoescape=True,
        extensions=[
            IncludeRawExtension,
        ],
    )
    env.filters |= {  # pyright: ignore[reportGeneralTypeIssues]
        "hexdoc_block": hexdoc_block,
        "hexdoc_wrap": hexdoc_wrap,
        "hexdoc_localize": hexdoc_localize,
        "hexdoc_texture_url": hexdoc_texture_url,
    }

    template = env.get_template(props.template.main)

    # render everything

    assert (output_dir := args.output_dir)
    if args.clean:
        shutil.rmtree(output_dir, ignore_errors=True)

    if args.update_latest:
        render_books(
            props=props,
            books=books,
            template=template,
            output_dir=output_dir,
            mod_metadata=mod_metadata,
            allow_missing=args.allow_missing,
            version="latest",
            is_root=False,
        )

    if args.is_release:
        render_books(
            props=props,
            books=books,
            template=template,
            output_dir=output_dir,
            mod_metadata=mod_metadata,
            allow_missing=args.allow_missing,
            version=version,
            is_root=False,
        )

    # the default book should be the latest released version
    if args.update_latest and args.is_release:
        render_books(
            props=props,
            books=books,
            template=template,
            output_dir=output_dir,
            mod_metadata=mod_metadata,
            allow_missing=args.allow_missing,
            version=version,
            is_root=True,
        )

    logger.info("Done.")


def render_books(
    *,
    props: Properties,
    books: dict[str, tuple[Book, I18n]],
    template: Template,
    output_dir: Path,
    mod_metadata: dict[str, HexdocMetadata],
    allow_missing: bool,
    version: str,
    is_root: bool,
):
    for lang, (book, i18n) in books.items():
        # /index.html
        # /lang/index.html
        # /v/version/index.html
        # /v/version/lang/index.html
        path = Path()
        if not is_root:
            path /= "v"
            path /= version
        if lang != props.default_lang:
            path /= lang

        output_dir /= path
        page_url = "/".join([props.url, *path.parts])

        logging.getLogger(__name__).info(f"Rendering {output_dir}")

        raw_docs = template.render(
            **props.template.args,
            book=book,
            props=props,
            page_url=page_url,
            version=version,
            lang=lang,
            mod_metadata=mod_metadata,
            is_bleeding_edge=version == "latest",
            # i18n helper
            _=lambda key: hexdoc_localize(
                key,
                do_format=False,
                props=props,
                book=book,
                i18n=i18n,
                allow_missing=allow_missing,
            ),
            # i18n helper, but with patchi formatting
            _f=lambda key: hexdoc_localize(
                key,
                do_format=True,
                props=props,
                book=book,
                i18n=i18n,
                allow_missing=allow_missing,
            ),
        )
        docs = strip_empty_lines(raw_docs)

        write_to_path(output_dir / "index.html", docs)
        if props.template.static_dir:
            shutil.copytree(props.template.static_dir, output_dir, dirs_exist_ok=True)

        # marker file for updating the sitemap later
        # we use this because matrix doesn't have outputs
        # this feels scuffed but it does work
        if not is_root:
            marker = SitemapMarker(
                version=version,
                lang=lang,
                path="/" + "/".join(path.parts),
                is_default_lang=lang == props.default_lang,
            )
            (output_dir / MARKER_NAME).write_text(marker.model_dump_json())


if __name__ == "__main__":
    main()
