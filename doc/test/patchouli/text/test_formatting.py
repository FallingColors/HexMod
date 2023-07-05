# pyright: reportPrivateUsage=false
from patchouli.text import DEFAULT_MACROS, FormatTree, Style


def test_format_string():
    # arrange
    test_str = "Write the given iota to my $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$().$(br)The $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$() is a lot like a $(l:items/focus)$(#b0b)Focus$(). It's cleared when I stop casting a Hex, starts with $(l:casting/influences)$(#490)Null$() in it, and is preserved between casts of $(l:patterns/meta#hexcasting:for_each)$(#fc77be)Thoth's Gambit$(). "

    # act
    tree = FormatTree.format(test_str, DEFAULT_MACROS)

    # assert
    # TODO: possibly make this less lazy
    assert tree == FormatTree(
        style=Style(type="base", value=True),
        children=[
            FormatTree(
                style=Style(type="para", value={}),
                children=[
                    "Write the given iota to my ",
                    FormatTree(
                        style=Style(
                            type="link",
                            value="patterns/readwrite#hexcasting:write/local",
                        ),
                        children=[
                            FormatTree(
                                style=Style(type="color", value="490"),
                                children=["local"],
                            )
                        ],
                    ),
                    ".\nThe ",
                    FormatTree(
                        style=Style(
                            type="link",
                            value="patterns/readwrite#hexcasting:write/local",
                        ),
                        children=[
                            FormatTree(
                                style=Style(type="color", value="490"),
                                children=["local"],
                            )
                        ],
                    ),
                    " is a lot like a ",
                    FormatTree(
                        style=Style(type="link", value="items/focus"),
                        children=[
                            FormatTree(
                                style=Style(type="color", value="b0b"),
                                children=["Focus"],
                            )
                        ],
                    ),
                    ". It's cleared when I stop casting a Hex, starts with ",
                    FormatTree(
                        style=Style(type="link", value="casting/influences"),
                        children=[
                            FormatTree(
                                style=Style(type="color", value="490"),
                                children=["Null"],
                            )
                        ],
                    ),
                    " in it, and is preserved between casts of ",
                    FormatTree(
                        style=Style(
                            type="link", value="patterns/meta#hexcasting:for_each"
                        ),
                        children=[
                            FormatTree(
                                style=Style(type="color", value="fc77be"),
                                children=["Thoth's Gambit"],
                            )
                        ],
                    ),
                    ". ",
                ],
            )
        ],
    )
