import logging
import subprocess
from pathlib import Path

from hexdoc.core.loader import ModResourceLoader
from hexdoc.core.metadata import HexdocMetadata
from hexdoc.core.properties import Properties
from hexdoc.hexcasting.hex_book import load_hex_book
from hexdoc.minecraft import I18n
from hexdoc.minecraft.assets import Texture
from hexdoc.patchouli import Book
from hexdoc.plugin import PluginManager

from .logging import setup_logging


def load_common_data(props_file: Path, verbosity: int):
    setup_logging(verbosity)

    props = Properties.load(props_file)
    pm = PluginManager()

    version = load_version(props, pm)
    return props, pm, version


def load_version(props: Properties, pm: PluginManager):
    version = pm.mod_version(props.modid)
    logging.getLogger(__name__).info(f"Loading hexdoc for {props.modid} {version}")
    return version


def load_all_metadata(props: Properties, pm: PluginManager, loader: ModResourceLoader):
    version = pm.mod_version(props.modid)
    root = Path(
        subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            capture_output=True,
            encoding="utf-8",
        ).stdout.strip()
    )

    # this mod's metadata
    metadata = HexdocMetadata(
        book_url=f"{props.url}/v/{version}",
        asset_url=props.env.asset_url,
        textures=list(Texture.load_all(root, loader)),
    )

    loader.export(
        metadata.path(props.modid),
        metadata.model_dump_json(
            by_alias=True,
            warnings=False,
            exclude_defaults=True,
        ),
    )

    return loader.load_metadata(model_type=HexdocMetadata) | {props.modid: metadata}


def load_book(
    props: Properties,
    pm: PluginManager,
    lang: str | None,
    allow_missing: bool,
):
    """lang, book, i18n"""
    if lang is None:
        lang = props.default_lang

    with ModResourceLoader.clean_and_load_all(props, pm) as loader:
        all_metadata = load_all_metadata(props, pm, loader)
        i18n = _load_i18n(loader, None, allow_missing)[lang]

        _, data = Book.load_book_json(loader, props.book)
        book = load_hex_book(data, pm, loader, i18n, all_metadata)

    return lang, book, i18n


def load_books(
    props: Properties,
    pm: PluginManager,
    lang: str | None,
    allow_missing: bool,
):
    """books, all_metadata"""

    with ModResourceLoader.clean_and_load_all(props, pm) as loader:
        all_metadata = load_all_metadata(props, pm, loader)

        _, book_data = Book.load_book_json(loader, props.book)
        books = dict[str, tuple[Book, I18n]]()

        for lang, i18n in _load_i18n(loader, lang, allow_missing).items():
            book = load_hex_book(book_data, pm, loader, i18n, all_metadata)
            books[lang] = (book, i18n)
            loader.export_dir = None  # only export the first (default) book

        return books, all_metadata


def _load_i18n(
    loader: ModResourceLoader,
    lang: str | None,
    allow_missing: bool,
) -> dict[str, I18n]:
    # only load the specified language
    if lang is not None:
        i18n = I18n.load(
            loader,
            lang=lang,
            allow_missing=allow_missing,
        )
        return {lang: i18n}

    # load everything
    per_lang_i18n = I18n.load_all(
        loader,
        allow_missing=allow_missing,
    )

    # ensure the default lang is loaded first
    default_lang = loader.props.default_lang
    default_i18n = per_lang_i18n.pop(default_lang)

    return {default_lang: default_i18n} | per_lang_i18n
