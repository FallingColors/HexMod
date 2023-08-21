# pyright: reportPrivateUsage=false
from argparse import Namespace
from typing import cast

from hexdoc.minecraft.i18n import I18n
from hexdoc.patchouli.text import DEFAULT_MACROS, FormatTree
from hexdoc.patchouli.text.formatting import (
    CommandStyle,
    FunctionStyle,
    FunctionStyleType,
    ParagraphStyle,
    SpecialStyleType,
)
from hexdoc.utils.properties import Properties


def test_format_string():
    # arrange
    test_str = "Write the given iota to my $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$().$(br)The $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$() is a lot like a $(l:items/focus)$(#b0b)Focus$(). It's cleared when I stop casting a Hex, starts with $(l:casting/influences)$(#490)Null$() in it, and is preserved between casts of $(l:patterns/meta#hexcasting:for_each)$(#fc77be)Thoth's Gambit$(). "
    mock_i18n = cast(I18n, Namespace(keys={}))
    mock_props = cast(Properties, Namespace(is_0_black=False, i18n=mock_i18n))

    # act
    tree = FormatTree.format(test_str, DEFAULT_MACROS, mock_props, mock_i18n)

    # assert
    # TODO: possibly make this less lazy
    assert tree == FormatTree(
        style=CommandStyle(type=SpecialStyleType.base),
        children=[
            FormatTree(
                style=ParagraphStyle.paragraph(),
                children=[
                    "Write the given iota to my ",
                    FormatTree(
                        style=FunctionStyle(
                            type=FunctionStyleType.link,
                            value="patterns/readwrite#hexcasting:write/local",
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
                        style=FunctionStyle(
                            type=FunctionStyleType.link,
                            value="patterns/readwrite#hexcasting:write/local",
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
                        style=FunctionStyle(
                            type=FunctionStyleType.link,
                            value="items/focus",
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
                        style=FunctionStyle(
                            type=FunctionStyleType.link,
                            value="casting/influences",
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
                        style=FunctionStyle(
                            type=FunctionStyleType.link,
                            value="patterns/meta#hexcasting:for_each",
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
