#!/usr/bin/env python3
import io
from typing import Any

from common.properties import Properties
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
from patchouli.text.html import HTMLStream


def entry_spoilered(book: HexBook, entry: Entry):
    if entry.advancement is None:
        return False
    return entry.advancement in book.props.spoilers


def category_spoilered(book: HexBook, category: Category):
    return all(entry_spoilered(book, ent) for ent in category.entries)


def write_block(out: HTMLStream, block: FormatTree | str | LocalizedStr):
    if isinstance(block, LocalizedStr):
        block = str(block)
    if isinstance(block, str):
        first = False
        for line in block.split("\n"):
            if first:
                out.void_element("br")
            first = True
            out.text(line)
        return
    with block.style.element(out):
        for child in block.children:
            write_block(out, child)


def anchor_toc(out: HTMLStream):
    with out.element(
        "a",
        href="#table-of-contents",
        class_name="permalink small",
        title="Jump to top",
    ):
        out.empty_element("i", class_name="bi bi-box-arrow-up")


def permalink(out: HTMLStream, link: str):
    with out.element("a", href=link, class_name="permalink small", title="Permalink"):
        out.empty_element("i", class_name="bi bi-link-45deg")


def write_page(out: HTMLStream, entry_id: str, page: Page[Any], props: Properties):
    if anchor := page.anchor:
        anchor_id = entry_id + "@" + anchor
    else:
        anchor_id = None

    # TODO: put this in the page classes - this is just a stopgap to make the tests pass
    with out.element_if(anchor_id is not None, "div", id=anchor_id):
        if isinstance(page, PageWithTitle) and page.title is not None:
            # gross
            _kwargs = (
                {"class_name": "pattern-title"}
                if isinstance(page, LookupPatternPage)
                else {}
            )
            with out.element("h4", **_kwargs):
                out.text(page.title)
                if anchor_id:
                    permalink(out, "#" + anchor_id)

        match page:
            case EmptyPage():
                pass
            case LinkPage():
                write_block(out, page.text)
                with out.element("h4", class_name="linkout"):
                    with out.element("a", href=page.url):
                        out.text(page.link_text)
            case TextPage():
                # LinkPage is a TextPage, so this needs to be below it
                write_block(out, page.text)
            case SpotlightPage():
                with out.element("h4", class_name="spotlight-title page-header"):
                    out.text(page.item)
                if page.text is not None:
                    write_block(out, page.text)
            case CraftingPage():
                with out.element("blockquote", class_name="crafting-info"):
                    out.text(f"Depicted in the book: The crafting recipe for the ")
                    first = True
                    for recipe in page.recipes:
                        if not first:
                            out.text(" and ")
                        first = False
                        with out.element("code"):
                            out.text(recipe.result.item)
                    out.text(".")
                if page.text is not None:
                    write_block(out, page.text)
            case ImagePage():
                with out.element("p", class_name="img-wrapper"):
                    for img in page.images:
                        # TODO: make a thing for this
                        out.empty_element(
                            "img",
                            src=f"{props.base_asset_urls[img.namespace]}/assets/{img.namespace}/{img.path}",
                        )
                if page.text is not None:
                    write_block(out, page.text)
            case CraftingMultiPage():
                with out.element("blockquote", class_name="crafting-info"):
                    out.text(
                        f"Depicted in the book: Several crafting recipes, for the "
                    )
                    with out.element("code"):
                        out.text(page.recipes[0].result.item)
                    for i in page.recipes[1:]:
                        out.text(", ")
                        with out.element("code"):
                            out.text(i.result.item)
                    out.text(".")
                if page.text is not None:
                    write_block(out, page.text)
            case BrainsweepPage():
                with out.element("blockquote", class_name="crafting-info"):
                    out.text(
                        f"Depicted in the book: A mind-flaying recipe producing the "
                    )
                    with out.element("code"):
                        out.text(page.recipe.result.name)
                    out.text(".")
                if page.text is not None:
                    write_block(out, page.text)
            case PageWithPattern():
                with out.element("details", class_name="spell-collapsible"):
                    out.empty_element("summary", class_name="collapse-spell")
                    for pattern in page.patterns:
                        with out.element(
                            "canvas",
                            class_name="spell-viz",
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
                with out.element("p", class_name="todo-note"):
                    out.text(f"TODO: Missing processor for type: {type(page)}")
                if isinstance(page, PageWithText):
                    write_block(out, page.text)
    out.void_element("br")


def write_entry(out: HTMLStream, book: HexBook, entry: Entry):
    with out.element("div", id=entry.id.path):
        with out.element_if(
            entry_spoilered(book, entry),
            "div",
            class_name="spoilered",
        ):
            with out.element("h3", class_name="entry-title page-header"):
                write_block(out, entry.name)
                anchor_toc(out)
                permalink(out, entry.id.href)
            for page in entry.pages:
                write_page(out, entry.id.path, page, book.props)


def write_category(out: HTMLStream, book: HexBook, category: Category):
    with out.element("section", id=category.id.path):
        with out.element_if(
            category_spoilered(book, category),
            "div",
            class_name="spoilered",
        ):
            with out.element("h2", class_name="category-title page-header"):
                write_block(out, category.name)
                anchor_toc(out)
                permalink(out, category.id.href)
            write_block(out, category.description)
        for entry in category.entries:
            if entry.id not in book.props.blacklist:
                write_entry(out, book, entry)


def write_toc(out: HTMLStream, book: HexBook):
    with out.element("h2", id="table-of-contents", class_name="page-header"):
        out.text("Table of Contents")
        with out.element(
            "a",
            href="javascript:void(0)",
            class_name="permalink toggle-link small",
            data_target="toc-category",
            title="Toggle all",
        ):
            out.empty_element("i", class_name="bi bi-list-nested")
        permalink(out, "#table-of-contents")
    for category in book.categories.values():
        with out.element("details", class_name="toc-category"):
            with out.element("summary"):
                with out.element(
                    "a",
                    href=category.id.href,
                    class_name="spoilered"
                    if category_spoilered(book, category)
                    else "",
                ):
                    out.text(category.name)
            with out.element("ul"):
                for entry in category.entries:
                    with out.element("li"):
                        with out.element(
                            "a",
                            href=entry.id.href,
                            class_name="spoilered"
                            if entry_spoilered(book, entry)
                            else "",
                        ):
                            out.text(entry.name)


def write_book(out: HTMLStream, book: HexBook):
    with out.element("div", class_name="container"):
        with out.element("header", class_name="jumbotron"):
            with out.element("h1", class_name="book-title"):
                write_block(out, book.name)
            write_block(out, book.landing_text)
        with out.element("nav"):
            write_toc(out, book)
        with out.element("main", class_name="book-body"):
            for category in book.categories.values():
                write_category(out, book, category)


def generate_docs(book: HexBook, template: str) -> str:
    # FIXME: super hacky temporary solution for returning this as a string
    # just pass a string buffer to everything instead of a file
    with io.StringIO() as output:
        # TODO: refactor
        for line in template.splitlines(True):
            if line == "#DUMP_BODY_HERE\n":
                write_book(HTMLStream(output), book)
                output.write("\n")
            else:
                output.write(line)

        return output.getvalue()
