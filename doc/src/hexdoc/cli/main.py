import json
import logging
import os
import shutil
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
from typing import Annotated, Union

import typer

from hexdoc.minecraft import I18n
from hexdoc.utils import ModResourceLoader

from .load import load_book, load_books, load_common_data
from .render import create_jinja_env, render_book
from .sitemap import (
    assert_version_exists,
    delete_root_book,
    delete_updated_books,
    dump_sitemap,
    load_sitemap,
)

VerbosityOption = Annotated[int, typer.Option("--verbose", "-v", count=True)]
RequiredPathOption = Annotated[Path, typer.Option()]
UpdateLatestOption = Annotated[bool, typer.Option(envvar="UPDATE_LATEST")]
ReleaseOption = Annotated[bool, typer.Option(envvar="RELEASE")]

app = typer.Typer()


@app.command()
def list_langs(
    props_file: Path,
    *,
    verbosity: VerbosityOption = 0,
):
    """Get the available language codes as a JSON list."""
    props, pm, _ = load_common_data(props_file, verbosity)
    with ModResourceLoader.load_all(props, pm, export=False) as loader:
        langs = sorted(I18n.list_all(loader))
        print(json.dumps(langs))


@app.command()
def export(
    props_file: Path,
    *,
    lang: Union[str, None] = None,
    allow_missing: bool = False,
    verbosity: VerbosityOption = 0,
):
    """Run hexdoc, but skip rendering the web book - just export the book resources."""
    props, pm, _ = load_common_data(props_file, verbosity)
    load_book(props, pm, lang, allow_missing)


@app.command()
def render(
    props_file: Path,
    output_dir: Path,
    *,
    update_latest: UpdateLatestOption = True,
    release: ReleaseOption = False,
    clean: bool = False,
    lang: Union[str, None] = None,
    allow_missing: bool = False,
    verbosity: VerbosityOption = 0,
):
    """Export resources and render the web book."""

    # load data
    props, pm, version = load_common_data(props_file, verbosity)
    books, mod_metadata = load_books(props, pm, lang, allow_missing)

    logger = logging.getLogger(__name__)
    logger.info(f"update_latest={update_latest}, release={release}")

    # set up Jinja
    env = create_jinja_env(props)

    templates = {
        "index.html": env.get_template(props.template.main),
        "index.css": env.get_template(props.template.style),
    }

    if clean:
        shutil.rmtree(output_dir, ignore_errors=True)

    for should_render, version_, is_root in [
        (update_latest, "latest", False),
        (release, version, False),
        (update_latest and release, version, True),
    ]:
        if not should_render:
            continue
        for lang_, (book, i18n) in books.items():
            render_book(
                props=props,
                lang=lang_,
                book=book,
                i18n=i18n,
                templates=templates,
                output_dir=output_dir,
                mod_metadata=mod_metadata,
                allow_missing=allow_missing,
                version=version_,
                is_root=is_root,
            )

    logger.info("Done.")


@app.command()
def merge(
    *,
    src: RequiredPathOption,
    dst: RequiredPathOption,
    update_latest: UpdateLatestOption = True,
    release: ReleaseOption = False,
):
    # ensure at least the default language was built successfully
    if update_latest:
        assert_version_exists(root=src, version="latest")

    # TODO: figure out how to do this with pluggy (we don't have the props file here)
    # if is_release:
    #     assert_version_exists(src, GRADLE_VERSION)

    dst.mkdir(parents=True, exist_ok=True)

    # remove any stale data that we're about to replace
    delete_updated_books(src=src, dst=dst)
    if update_latest and release:
        delete_root_book(root=dst)

    # do the merge
    shutil.copytree(src=src, dst=dst, dirs_exist_ok=True)

    # rebuild the sitemap
    sitemap = load_sitemap(dst)
    dump_sitemap(dst, sitemap)


@app.command()
def serve(
    props_file: Path,
    *,
    port: int = 8000,
    src: RequiredPathOption,
    dst: RequiredPathOption,
    update_latest: bool = True,
    release: bool = False,
    clean: bool = False,
    lang: Union[str, None] = None,
    allow_missing: bool = False,
    verbosity: VerbosityOption = 0,
):
    book_path = dst.resolve().relative_to(Path.cwd())

    base_url = f"http://localhost:{port}"
    book_url = f"{base_url}/{book_path.as_posix()}"

    os.environ |= {
        "DEBUG_GITHUBUSERCONTENT": base_url,
        "GITHUB_PAGES_URL": book_url,
    }

    print("Rendering...")
    render(
        props_file=props_file,
        output_dir=src,
        update_latest=update_latest,
        release=release,
        clean=clean,
        lang=lang,
        allow_missing=allow_missing,
        verbosity=verbosity,
    )

    print("Merging...")
    merge(
        src=src,
        dst=dst,
        update_latest=update_latest,
        release=release,
    )

    print(f"Serving web book at {book_url} (press ctrl+c to exit)\n")
    with HTTPServer(("", port), SimpleHTTPRequestHandler) as httpd:
        httpd.serve_forever()


if __name__ == "__main__":
    app()
