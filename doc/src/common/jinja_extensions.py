from typing import Any

from jinja2 import nodes
from jinja2.ext import Extension
from jinja2.parser import Parser
from markupsafe import Markup

from minecraft.i18n import LocalizedStr
from patchouli.text.formatting import FormatTree
from patchouli.text.html import HTMLStream


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


def hexdoc_block(value: Any) -> str:
    match value:
        case LocalizedStr() | str():
            # use Markup to tell Jinja not to escape this string for us
            lines = str(value).splitlines()
            return Markup("<br />".join(Markup.escape(line) for line in lines))
        case FormatTree():
            with HTMLStream() as out:
                with value.style.element(out):
                    for child in value.children:
                        out.write(hexdoc_block(child))
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
