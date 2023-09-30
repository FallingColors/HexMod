import logging
from pathlib import Path

from hexdoc.hexcasting.hex_book import load_hex_book
from hexdoc.minecraft import I18n
from hexdoc.patchouli import Book
from hexdoc.plugin import PluginManager
from hexdoc.utils import ModResourceLoader, Properties

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
        lang, i18n = _load_i18n(loader, lang, allow_missing)[0]

        _, data = Book.load_book_json(loader, props.book)
        book = load_hex_book(data, pm, loader, i18n)

    return lang, book, i18n


def load_books(
    props: Properties,
    pm: PluginManager,
    lang: str | None,
    allow_missing: bool,
):
    """books, mod_metadata"""

    with ModResourceLoader.clean_and_load_all(props, pm) as loader:
        _, book_data = Book.load_book_json(loader, props.book)

        books = dict[str, tuple[Book, I18n]]()
        for lang, i18n in _load_i18n(loader, lang, allow_missing):
            books[lang] = (load_hex_book(book_data, pm, loader, i18n), i18n)
            loader.export_dir = None  # only export the first (default) book

        return books, loader.mod_metadata


def _load_i18n(
    loader: ModResourceLoader,
    lang: str | None,
    allow_missing: bool,
) -> list[tuple[str, I18n]]:
    # only load the specified language
    if lang is not None:
        i18n = I18n.load(
            loader,
            lang=lang,
            allow_missing=allow_missing,
        )
        return [(lang, i18n)]

    # load everything
    per_lang_i18n = I18n.load_all(
        loader,
        allow_missing=allow_missing,
    )

    # ensure the default lang is loaded first
    default_lang = loader.props.default_lang
    default_i18n = per_lang_i18n.pop(default_lang)

    return [(default_lang, default_i18n), *per_lang_i18n.items()]
