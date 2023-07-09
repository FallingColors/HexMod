#!/usr/bin/env python3
import io
from typing import Any

from hexcasting import HexBook
from hexcasting.abstract_hex_pages import PageWithPattern
from hexcasting.hex_pages import BrainsweepPage, CraftingMultiPage, LookupPatternPage
from minecraft.i18n import LocalizedStr
from patchouli import Category, Entry, FormatTree, Page
from patchouli.page import (
    CraftingPage,
    EmptyPage,
    ImagePage,
    LinkPage,
    PageWithText,
    PageWithTitle,
    SpotlightPage,
    TextPage,
)
from patchouli.text.tags import Stream

# extra info :(
# TODO: properties.toml
repo_names = {
    "hexcasting": "https://raw.githubusercontent.com/gamma-delta/HexMod/main/Common/src/main/resources",
}


def entry_spoilered(root_info: HexBook, entry: Entry):
    if entry.advancement is None:
        return False
    return str(entry.advancement) in root_info.context["spoilers"]


def category_spoilered(root_info: HexBook, category: Category):
    return all(entry_spoilered(root_info, ent) for ent in category.entries)


def write_block(out: Stream, block: FormatTree | str | LocalizedStr):
    if isinstance(block, LocalizedStr):
        block = str(block)
    if isinstance(block, str):
        first = False
        for line in block.split("\n"):
            if first:
                out.tag("br")
            first = True
            out.text(line)
        return
    with block.style.tag(out):
        for child in block.children:
            write_block(out, child)


def anchor_toc(out: Stream):
    with out.pair_tag(
        "a", href="#table-of-contents", clazz="permalink small", title="Jump to top"
    ):
        out.empty_pair_tag("i", clazz="bi bi-box-arrow-up")


def permalink(out: Stream, link: str):
    with out.pair_tag("a", href=link, clazz="permalink small", title="Permalink"):
        out.empty_pair_tag("i", clazz="bi bi-link-45deg")


def write_page(out: Stream, pageid: str, page: Page[Any]):
    if anchor := page.anchor:
        anchor_id = pageid + "@" + anchor
    else:
        anchor_id = None

    # TODO: put this in the page classes - this is just a stopgap to make the tests pass
    with out.pair_tag_if(anchor_id, "div", id=anchor_id):
        if isinstance(page, PageWithTitle) and page.title is not None:
            # gross
            _kwargs = (
                {"clazz": "pattern-title"}
                if isinstance(page, LookupPatternPage)
                else {}
            )
            with out.pair_tag("h4", **_kwargs):
                out.text(page.title)
                if anchor_id:
                    permalink(out, "#" + anchor_id)

        match page:
            case EmptyPage():
                pass
            case LinkPage():
                write_block(out, page.text)
                with out.pair_tag("h4", clazz="linkout"):
                    with out.pair_tag("a", href=page.url):
                        out.text(page.link_text)
            case TextPage():
                # LinkPage is a TextPage, so this needs to be below it
                write_block(out, page.text)
            case SpotlightPage():
                with out.pair_tag("h4", clazz="spotlight-title page-header"):
                    out.text(page.item)
                if page.text is not None:
                    write_block(out, page.text)
            case CraftingPage():
                with out.pair_tag("blockquote", clazz="crafting-info"):
                    out.text(f"Depicted in the book: The crafting recipe for the ")
                    first = True
                    for recipe in page.recipes:
                        if not first:
                            out.text(" and ")
                        first = False
                        with out.pair_tag("code"):
                            out.text(recipe.result.item)
                    out.text(".")
                if page.text is not None:
                    write_block(out, page.text)
            case ImagePage():
                with out.pair_tag("p", clazz="img-wrapper"):
                    for img in page.images:
                        # TODO: make a thing for this
                        out.empty_pair_tag(
                            "img",
                            src=f"{repo_names[img.namespace]}/assets/{img.namespace}/{img.path}",
                        )
                if page.text is not None:
                    write_block(out, page.text)
            case CraftingMultiPage():
                with out.pair_tag("blockquote", clazz="crafting-info"):
                    out.text(
                        f"Depicted in the book: Several crafting recipes, for the "
                    )
                    with out.pair_tag("code"):
                        out.text(page.recipes[0].result.item)
                    for i in page.recipes[1:]:
                        out.text(", ")
                        with out.pair_tag("code"):
                            out.text(i.result.item)
                    out.text(".")
                if page.text is not None:
                    write_block(out, page.text)
            case BrainsweepPage():
                with out.pair_tag("blockquote", clazz="crafting-info"):
                    out.text(
                        f"Depicted in the book: A mind-flaying recipe producing the "
                    )
                    with out.pair_tag("code"):
                        out.text(page.recipe.result.name)
                    out.text(".")
                if page.text is not None:
                    write_block(out, page.text)
            case PageWithPattern():
                with out.pair_tag("details", clazz="spell-collapsible"):
                    out.empty_pair_tag("summary", clazz="collapse-spell")
                    for pattern in page.patterns:
                        with out.pair_tag(
                            "canvas",
                            clazz="spell-viz",
                            width=216,
                            height=216,
                            data_string=pattern.signature,
                            data_start=pattern.startdir.name.lower(),
                            data_per_world=pattern.is_per_world,
                        ):
                            out.text(
                                "Your browser does not support visualizing patterns. Pattern code: "
                                + pattern.signature
                            )
                write_block(out, page.text)
            case _:
                with out.pair_tag("p", clazz="todo-note"):
                    out.text(f"TODO: Missing processor for type: {type(page)}")
                if isinstance(page, PageWithText):
                    write_block(out, page.text)
    out.tag("br")


def write_entry(out: Stream, book: HexBook, entry: Entry):
    with out.pair_tag("div", id=entry.id.path):
        with out.pair_tag_if(entry_spoilered(book, entry), "div", clazz="spoilered"):
            with out.pair_tag("h3", clazz="entry-title page-header"):
                write_block(out, entry.name)
                anchor_toc(out)
                permalink(out, entry.id.href)
            for page in entry.pages:
                write_page(out, entry.id.path, page)


def write_category(out: Stream, book: HexBook, category: Category):
    with out.pair_tag("section", id=category.id.path):
        with out.pair_tag_if(
            category_spoilered(book, category), "div", clazz="spoilered"
        ):
            with out.pair_tag("h2", clazz="category-title page-header"):
                write_block(out, category.name)
                anchor_toc(out)
                permalink(out, category.id.href)
            write_block(out, category.description)
        for entry in category.entries:
            if entry.id.path not in book.context["blacklist"]:
                write_entry(out, book, entry)


def write_toc(out: Stream, book: HexBook):
    with out.pair_tag("h2", id="table-of-contents", clazz="page-header"):
        out.text("Table of Contents")
        with out.pair_tag(
            "a",
            href="javascript:void(0)",
            clazz="permalink toggle-link small",
            data_target="toc-category",
            title="Toggle all",
        ):
            out.empty_pair_tag("i", clazz="bi bi-list-nested")
        permalink(out, "#table-of-contents")
    for category in book.categories.values():
        with out.pair_tag("details", clazz="toc-category"):
            with out.pair_tag("summary"):
                with out.pair_tag(
                    "a",
                    href=category.id.href,
                    clazz="spoilered" if category_spoilered(book, category) else "",
                ):
                    out.text(category.name)
            with out.pair_tag("ul"):
                for entry in category.entries:
                    with out.pair_tag("li"):
                        with out.pair_tag(
                            "a",
                            href=entry.id.href,
                            clazz="spoilered" if entry_spoilered(book, entry) else "",
                        ):
                            out.text(entry.name)


def write_book(out: Stream, book: HexBook):
    with out.pair_tag("div", clazz="container"):
        with out.pair_tag("header", clazz="jumbotron"):
            with out.pair_tag("h1", clazz="book-title"):
                write_block(out, book.name)
            write_block(out, book.landing_text)
        with out.pair_tag("nav"):
            write_toc(out, book)
        with out.pair_tag("main", clazz="book-body"):
            for category in book.categories.values():
                write_category(out, book, category)


def generate_docs(book: HexBook, template: str) -> str:
    # FIXME: super hacky temporary solution for returning this as a string
    # just pass a string buffer to everything instead of a file
    with io.StringIO() as output:
        # TODO: refactor
        for line in template.splitlines(True):
            if line.startswith("#DO_NOT_RENDER"):
                _, *blacklist = line.split()
                book.context["blacklist"].update(blacklist)

            if line.startswith("#SPOILER"):
                _, *spoilers = line.split()
                book.context["spoilers"].update(spoilers)
            elif line == "#DUMP_BODY_HERE\n":
                write_book(Stream(output), book)
                print("", file=output)
            else:
                print(line, end="", file=output)

        return output.getvalue()
