# pyright: reportPrivateUsage=false
from patchouli.text import DEFAULT_MACROS, FormatTree
from patchouli.text.formatting import ClearStyle, ColorStyle, LinkStyle, ParagraphStyle


def test_format_string():
    # arrange
    test_str = "Write the given iota to my $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$().$(br)The $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$() is a lot like a $(l:items/focus)$(#b0b)Focus$(). It's cleared when I stop casting a Hex, starts with $(l:casting/influences)$(#490)Null$() in it, and is preserved between casts of $(l:patterns/meta#hexcasting:for_each)$(#fc77be)Thoth's Gambit$(). "

    # act
    tree = FormatTree.format(test_str, DEFAULT_MACROS)

    # assert
    # TODO: possibly make this less lazy
    assert tree == FormatTree(
        style=ClearStyle(),
        children=[
            FormatTree(
                style=ParagraphStyle(),
                children=[
                    "Write the given iota to my ",
                    FormatTree(
                        style=LinkStyle("patterns/readwrite#hexcasting:write/local"),
                        children=[
                            FormatTree(
                                style=ColorStyle("490"),
                                children=["local"],
                            )
                        ],
                    ),
                    ".\nThe ",
                    FormatTree(
                        style=LinkStyle("patterns/readwrite#hexcasting:write/local"),
                        children=[
                            FormatTree(
                                style=ColorStyle("490"),
                                children=["local"],
                            )
                        ],
                    ),
                    " is a lot like a ",
                    FormatTree(
                        style=LinkStyle("items/focus"),
                        children=[
                            FormatTree(
                                style=ColorStyle("b0b"),
                                children=["Focus"],
                            )
                        ],
                    ),
                    ". It's cleared when I stop casting a Hex, starts with ",
                    FormatTree(
                        style=LinkStyle("casting/influences"),
                        children=[
                            FormatTree(
                                style=ColorStyle("490"),
                                children=["Null"],
                            )
                        ],
                    ),
                    " in it, and is preserved between casts of ",
                    FormatTree(
                        style=LinkStyle("patterns/meta#hexcasting:for_each"),
                        children=[
                            FormatTree(
                                style=ColorStyle("fc77be"),
                                children=["Thoth's Gambit"],
                            )
                        ],
                    ),
                    ". ",
                ],
            )
        ],
    )
