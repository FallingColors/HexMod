# pyright: reportPrivateUsage=false
from argparse import Namespace
from typing import cast

from hexdoc.minecraft.i18n import I18n
from hexdoc.patchouli.text import DEFAULT_MACROS, FormatTree
from hexdoc.patchouli.text.formatting import (
    BookLink,
    CommandStyle,
    FormattingContext,
    FunctionStyle,
    LinkStyle,
    ParagraphStyle,
    SpecialStyleType,
)
from hexdoc.utils.properties import Properties
from hexdoc.utils.resource import ResourceLocation


def test_format_string():
    # arrange
    test_str = "Write the given iota to my $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$().$(br)The $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$() is a lot like a $(l:items/focus)$(#b0b)Focus$(). It's cleared when I stop casting a Hex, starts with $(l:casting/influences)$(#490)Null$() in it, and is preserved between casts of $(l:patterns/meta#hexcasting:for_each)$(#fc77be)Thoth's Gambit$(). "
    mock_i18n = cast(I18n, Namespace(keys={}))
    mock_props = cast(
        Properties,
        Namespace(
            is_0_black=False,
            book=ResourceLocation("hexcasting", "thehexbook"),
        ),
    )
    mock_context = cast(
        FormattingContext,
        Namespace(
            i18n=mock_i18n,
            props=mock_props,
            macros=DEFAULT_MACROS,
            links_to_check=[],
        ),
    )

    # act
    tree = FormatTree.format(test_str, mock_context)

    # assert
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
