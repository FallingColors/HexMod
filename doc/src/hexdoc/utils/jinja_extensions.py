from typing import Any

from jinja2 import nodes, pass_context
from jinja2.ext import Extension
from jinja2.parser import Parser
from jinja2.runtime import Context
from markupsafe import Markup

from hexdoc.minecraft import I18n, LocalizedStr
from hexdoc.patchouli import Book, FormatTree
from hexdoc.patchouli.book import Book
from hexdoc.patchouli.text import HTMLStream
from hexdoc.patchouli.text.formatting import FormatTree
from hexdoc.utils.resource import ResourceLocation
from hexdoc.utils.resource_loader import HexdocMetadata

from . import Properties
from .deserialize import cast_or_raise


# https://stackoverflow.com/a/64392515
class IncludeRawExtension(Extension):
    tags = {"include_raw"}

    def parse(self, parser: Parser) -> nodes.Node:
        lineno = parser.stream.expect("name:include_raw").lineno
        template = parser.parse_expression()
        result = self.call_method("_render", [template], lineno=lineno)
        return nodes.Output([result], lineno=lineno)

    def _render(self, filename: str) -> Markup:
        assert self.environment.loader is not None
        source = self.environment.loader.get_source(self.environment, filename)
        return Markup(source[0])


@pass_context
def hexdoc_block(context: Context | dict[{"book": Book}], value: Any) -> str:
    try:
        book = cast_or_raise(context["book"], Book)
        return _hexdoc_block(book, value)
    except Exception as e:
        e.add_note(f"Value:\n    {value}")
        raise


def _hexdoc_block(book: Book, value: Any) -> str:
    match value:
        case LocalizedStr() | str():
            # use Markup to tell Jinja not to escape this string for us
            lines = str(value).splitlines()
            return Markup("<br />".join(Markup.escape(line) for line in lines))

        case FormatTree():
            with HTMLStream() as out:
                with value.style.element(out, book.link_bases):
                    for child in value.children:
                        out.write(_hexdoc_block(book, child))
                return Markup(out.getvalue())

        case None:
            return ""
        case _:
            raise TypeError(value)


def hexdoc_wrap(value: str, *args: str):
    tag, *attributes = args
    if attributes:
        attributes = " " + " ".join(attributes)
    else:
        attributes = ""
    return Markup(f"<{tag}{attributes}>{Markup.escape(value)}</{tag}>")


# aliased as _() and _f() at render time
def hexdoc_localize(
    key: str,
    *,
    do_format: bool,
    props: Properties,
    book: Book,
    i18n: I18n,
    allow_missing: bool,
):
    # get the localized value from i18n
    localized = i18n.localize(key, allow_missing=allow_missing)

    if not do_format:
        return Markup(localized.value)

    # construct a FormatTree from the localized value (to allow using patchi styles)
    formatted = FormatTree.format(
        localized.value,
        book_id=book.id,
        i18n=i18n,
        macros=book.macros,
        is_0_black=props.is_0_black,
    )
    return Markup(hexdoc_block({"book": book}, formatted))


@pass_context
def hexdoc_texture_url(context: Context, id: ResourceLocation) -> str:
    try:
        metadata = cast_or_raise(
            context["mod_metadata"],
            dict[str, HexdocMetadata],
        )[id.namespace]
        return f"{metadata.asset_url}/{metadata.textures[id].as_posix()}"
    except Exception as e:
        e.add_note(f"id:\n    {id}")
        raise
