from typing import Any

from jinja2 import pass_context
from jinja2.runtime import Context
from markupsafe import Markup
from pydantic import ConfigDict, validate_call

from hexdoc.core.properties import Properties
from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import I18n, LocalizedStr
from hexdoc.minecraft.assets import Texture
from hexdoc.patchouli import Book, FormatTree
from hexdoc.patchouli.text.formatting import BookLinkBases, HTMLStream
from hexdoc.utils.deserialize import cast_or_raise


@pass_context
def hexdoc_block(context: Context | BookLinkBases, value: Any) -> str:
    try:
        if isinstance(context, dict):
            link_bases = context
        else:
            link_bases = cast_or_raise(context["book"], Book).link_bases
        return _hexdoc_block(link_bases, value)
    except Exception as e:
        e.add_note(f"Value:\n    {value}")
        raise


def _hexdoc_block(link_bases: BookLinkBases, value: Any) -> str:
    match value:
        case LocalizedStr() | str():
            # use Markup to tell Jinja not to escape this string for us
            lines = str(value).splitlines()
            return Markup("<br />".join(Markup.escape(line) for line in lines))

        case FormatTree():
            with HTMLStream() as out:
                with value.style.element(out, link_bases):
                    for child in value.children:
                        out.write(_hexdoc_block(link_bases, child))
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
    return Markup(hexdoc_block(book.link_bases, formatted))


@pass_context
@validate_call(config=ConfigDict(arbitrary_types_allowed=True))
def hexdoc_texture(context: Context, id: ResourceLocation) -> str:
    try:
        props = cast_or_raise(context["props"], Properties)
        textures = cast_or_raise(context["textures"], dict[Any, Any])
        texture = Texture.find(id, props=props, textures=textures)

        assert texture.url is not None
        return texture.url
    except Exception as e:
        e.add_note(f"id:\n    {id}")
        raise
