import json
import logging
import os
import shutil
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
from typing import Annotated, Union

import typer

from hexdoc.core.loader import ModResourceLoader
from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import I18n
from hexdoc.minecraft.assets.textures import AnimatedTexture, Texture

from .utils.load import load_book, load_books, load_common_data
from .utils.render import create_jinja_env, render_book
from .utils.sitemap import (
    assert_version_exists,
    delete_root_book,
    delete_updated_books,
    dump_sitemap,
    load_sitemap,
)

PathArgument = Annotated[Path, typer.Argument()]
VerbosityOption = Annotated[int, typer.Option("--verbose", "-v", count=True)]
UpdateLatestOption = Annotated[bool, typer.Option(envvar="UPDATE_LATEST")]
ReleaseOption = Annotated[bool, typer.Option(envvar="RELEASE")]

DEFAULT_PROPS_FILE = Path("doc/properties.toml")
DEFAULT_MERGE_SRC = Path("_site/src/docs")
DEFAULT_MERGE_DST = Path("_site/dst/docs")

app = typer.Typer(pretty_exceptions_enable=False)


@app.command()
def list_langs(
    props_file: PathArgument = DEFAULT_PROPS_FILE,
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
    props_file: PathArgument = DEFAULT_PROPS_FILE,
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
    props_file: PathArgument = DEFAULT_PROPS_FILE,
    output_dir: PathArgument = DEFAULT_MERGE_SRC,
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
    books, all_metadata = load_books(props, pm, lang, allow_missing)

    textures = dict[ResourceLocation, Texture]()
    animations = list[AnimatedTexture]()

    for metadata in all_metadata.values():
        for texture in metadata.textures:
            textures[texture.file_id] = texture
            if isinstance(texture, AnimatedTexture):
                animations.append(texture)

    # sort the animations to hopefully avoid flaky tests
    animations.sort(key=lambda t: t.class_name)

    logger = logging.getLogger(__name__)
    logger.info(f"update_latest={update_latest}, release={release}")

    # set up Jinja
    env = create_jinja_env(pm, props.template.include)

    templates = {
        Path(path): env.get_template(template_name)
        for path, template_name in props.template.render.items()
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
                pm=pm,
                lang=lang_,
                book=book,
                i18n=i18n,
                templates=templates,
                output_dir=output_dir,
                all_metadata=all_metadata,
                textures=textures,
                animations=animations,
                allow_missing=allow_missing,
                version=version_,
                is_root=is_root,
            )

    logger.info("Done.")


@app.command()
def merge(
    *,
    src: Path = DEFAULT_MERGE_SRC,
    dst: Path = DEFAULT_MERGE_DST,
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
    props_file: PathArgument = DEFAULT_PROPS_FILE,
    *,
    port: int = 8000,
    src: Path = DEFAULT_MERGE_SRC,
    dst: Path = DEFAULT_MERGE_DST,
    update_latest: bool = True,
    release: bool = True,  # you'd generally want --release for development
    clean: bool = False,
    lang: Union[str, None] = None,
    allow_missing: bool = False,
    verbosity: VerbosityOption = 0,
):
    book_path = dst.resolve().relative_to(Path.cwd())

    book_url = f"/{book_path.as_posix()}"

    os.environ |= {
        "DEBUG_GITHUBUSERCONTENT": "",
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

    print(
        f"Serving web book at http://localhost:{port}{book_url} (press ctrl+c to exit)\n"
    )
    with HTTPServer(("", port), SimpleHTTPRequestHandler) as httpd:
        # ignore KeyboardInterrupt to stop Typer from printing "Aborted."
        # because it keeps printing after nodemon exits and breaking the output
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            pass


if __name__ == "__main__":
    app()
