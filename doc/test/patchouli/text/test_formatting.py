# pyright: reportPrivateUsage=false
from argparse import Namespace
from typing import Any, cast

import pytest
from jinja2 import Environment, PackageLoader

from hexdoc.core.resource import ResourceLocation
from hexdoc.minecraft import I18n
from hexdoc.minecraft.i18n import I18n
from hexdoc.patchouli.text import DEFAULT_MACROS, FormatTree
from hexdoc.patchouli.text.formatting import (
    BookLink,
    CommandStyle,
    FunctionStyle,
    LinkStyle,
    ParagraphStyle,
    SpecialStyleType,
)
from hexdoc.plugin.manager import PluginManager


class MockPluginManager:
    def validate_format_tree(self, tree: FormatTree, *_: Any, **__: Any):
        return tree


def format_with_mocks(test_str: str, macros: dict[str, str] = {}):
    return FormatTree.format(
        test_str,
        book_id=ResourceLocation("hexcasting", "thehexbook"),
        i18n=cast(I18n, Namespace(keys={})),
        macros=DEFAULT_MACROS | macros,
        is_0_black=False,
        pm=cast(PluginManager, MockPluginManager()),
    )


def flatten_html(html: str):
    return "".join(line.lstrip() for line in html.splitlines())


def hexdoc_block(value: FormatTree):
    loader = PackageLoader("hexdoc", "_templates")
    env = Environment(loader=loader)
    template = env.from_string(
        """\
        {%- import "macros/formatting.html.jinja" as fmt with context -%}
        {{- fmt.styled(value) -}}
        """
    )
    return template.render(value=value)


def test_link():
    tree = format_with_mocks("$(l:http://google.com)A$(/l)")
    assert hexdoc_block(tree) == "<p><a href='http://google.com'>A</a></p>"


def test_link_in_color():
    tree = format_with_mocks(
        "$(1)A$(l:http://google.com)B$(/l)C/$",
        {"$(1)": "$(#111)"},
    )
    html = hexdoc_block(tree)

    assert html == flatten_html(
        """<p>
            <span style='color: #111'>
                A
                <a href='http://google.com'>
                    B
                </a>
                C
            </span>
        </p>"""
    )


@pytest.mark.skip("Currently failing, the parser needs a fix")
def test_colors_across_link():
    tree = format_with_mocks(
        "$(1)A$(l:http://google.com)B$(2)C$(1)D$(/l)E/$",
        {"$(1)": "$(#111)", "$(2)": "$(#222)"},
    )
    html = hexdoc_block(tree)

    assert html == flatten_html(
        """<p>
            <span style='color: #111'>
                A
            </span>
            <a href='http://google.com'>
                <span style='color: #222'>
                    C
                </span>
                <span style='color: #111'>
                    D
                </span>
            </a>
            <span style='color: #111'>
                E
            </span>
        </p>"""
    )


def test_format_string():
    tree = format_with_mocks(
        "Write the given iota to my $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$().$(br)The $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$() is a lot like a $(l:items/focus)$(#b0b)Focus$(). It's cleared when I stop casting a Hex, starts with $(l:casting/influences)$(#490)Null$() in it, and is preserved between casts of $(l:patterns/meta#hexcasting:for_each)$(#fc77be)Thoth's Gambit$(). "
    )

    assert tree == FormatTree(
        style=CommandStyle(type=SpecialStyleType.base),
        children=[
            FormatTree(
                style=ParagraphStyle.paragraph(),
                children=[
                    "Write the given iota to my ",
                    FormatTree(
                        style=LinkStyle(
                            value=BookLink.from_str(
                                "patterns/readwrite#hexcasting:write/local",
                                ResourceLocation("hexcasting", "thehexbook"),
                            ),
                        ),
                        children=[
                            FormatTree(
                                style=FunctionStyle(
                                    type=SpecialStyleType.color,
                                    value="490",
                                ),
                                children=["local"],
                            )
                        ],
                    ),
                    ".\nThe ",
                    FormatTree(
                        style=LinkStyle(
                            value=BookLink.from_str(
                                "patterns/readwrite#hexcasting:write/local",
                                ResourceLocation("hexcasting", "thehexbook"),
                            ),
                        ),
                        children=[
                            FormatTree(
                                style=FunctionStyle(
                                    type=SpecialStyleType.color,
                                    value="490",
                                ),
                                children=["local"],
                            )
                        ],
                    ),
                    " is a lot like a ",
                    FormatTree(
                        style=LinkStyle(
                            value=BookLink.from_str(
                                "items/focus",
                                ResourceLocation("hexcasting", "thehexbook"),
                            ),
                        ),
                        children=[
                            FormatTree(
                                style=FunctionStyle(
                                    type=SpecialStyleType.color,
                                    value="b0b",
                                ),
                                children=["Focus"],
                            )
                        ],
                    ),
                    ". It's cleared when I stop casting a Hex, starts with ",
                    FormatTree(
                        style=LinkStyle(
                            value=BookLink.from_str(
                                "casting/influences",
                                ResourceLocation("hexcasting", "thehexbook"),
                            ),
                        ),
                        children=[
                            FormatTree(
                                style=FunctionStyle(
                                    type=SpecialStyleType.color,
                                    value="490",
                                ),
                                children=["Null"],
                            )
                        ],
                    ),
                    " in it, and is preserved between casts of ",
                    FormatTree(
                        style=LinkStyle(
                            value=BookLink.from_str(
                                "patterns/meta#hexcasting:for_each",
                                ResourceLocation("hexcasting", "thehexbook"),
                            ),
                        ),
                        children=[
                            FormatTree(
                                style=FunctionStyle(
                                    type=SpecialStyleType.color,
                                    value="fc77be",
                                ),
                                children=["Thoth's Gambit"],
                            )
                        ],
                    ),
                    ". ",
                ],
            )
        ],
    )
