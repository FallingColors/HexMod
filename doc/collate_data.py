#!/usr/bin/env python3
from sys import argv, stdout
from collections import namedtuple
import json # codec
import re # parsing
import os # listdir

# TO USE: put in Hexcasting root dir, collate_data.py src/main/resources hexcasting thehexbook out.html

# extra info :(
lang = "en_us"
repo_names = {
    "hexcasting": "https://raw.githubusercontent.com/gamma-delta/HexMod/main/Common/src/main/resources",
}
extra_i18n = {
    "item.minecraft.amethyst_shard": "Amethyst Shard",
    "item.minecraft.budding_amethyst": "Budding Amethyst",
    "block.hexcasting.slate": "Blank Slate",
}

default_macros = {
    "$(obf)": "$(k)",
    "$(bold)": "$(l)",
    "$(strike)": "$(m)",
    "$(italic)": "$(o)",
    "$(italics)": "$(o)",
    "$(list": "$(li",
    "$(reset)": "$()",
    "$(clear)": "$()",
    "$(2br)": "$(br2)",
    "$(p)": "$(br2)",
    "/$": "$()",
    "<br>": "$(br)",
    "$(nocolor)": "$(0)",
    "$(item)": "$(#b0b)",
    "$(thing)": "$(#490)",
}

colors = {
    "0": None,
    "1": "00a",
    "2": "0a0",
    "3": "0aa",
    "4": "a00",
    "5": "a0a",
    "6": "fa0",
    "7": "aaa",
    "8": "555",
    "9": "55f",
    "a": "5f5",
    "b": "5ff",
    "c": "f55",
    "d": "f5f",
    "e": "ff5",
    "f": "fff",
}
types = {
    "k": "obf",
    "l": "bold",
    "m": "strikethrough",
    "n": "underline",
    "o": "italic",
}

keys = {
    "use": "Right Click",
    "sneak": "Left Shift",
}

bind1 = (lambda: None).__get__(0).__class__

def slurp(filename):
    with open(filename, "r") as fh:
        return json.load(fh)

FormatTree = namedtuple("FormatTree", ["style", "children"])
Style = namedtuple("Style", ["type", "value"])

def parse_style(sty):
    if sty == "br":
        return "<br />", None
    if sty == "br2":
        return "", Style("para", {})
    if sty == "li":
        return "", Style("para", {"clazz": "fake-li"})
    if sty[:2] == "k:":
        return keys[sty[2:]], None
    if sty[:2] == "l:":
        return "", Style("link", sty[2:])
    if sty == "/l":
        return "", Style("link", None)
    if sty == "playername":
        return "[Playername]", None
    if sty[:2] == "t:":
        return "", Style("tooltip", sty[2:])
    if sty == "/t":
        return "", Style("tooltip", None)
    if sty[:2] == "c:":
        return "", Style("cmd_click", sty[2:])
    if sty == "/c":
        return "", Style("cmd_click", None)
    if sty == "r" or not sty:
        return "", Style("base", None)
    if sty in types:
        return "", Style(types[sty], True)
    if sty in colors:   
        return "", Style("color", colors[sty])
    if sty.startswith("#") and len(sty) in [4, 7]:
        return "", Style("color", sty[1:])
    # TODO more style parse
    raise ValueError("Unknown style: " + sty)

def localize(i18n, string):
    return i18n.get(string, string) if i18n else string

format_re = re.compile(r"\$\(([^)]*)\)")
def format_string(root_data, string):
    # resolve lang
    string = localize(root_data["i18n"], string)
    # resolve macros
    old_string = None
    while old_string != string:
        old_string = string
        for macro, replace in root_data["macros"].items():
            string = string.replace(macro, replace)
        else: break

    # lex out parsed styles
    text_nodes = []
    styles = []
    last_end = 0
    extra_text = ""
    for mobj in re.finditer(format_re, string):
        bonus_text, sty = parse_style(mobj.group(1))
        text = string[last_end:mobj.start()] + bonus_text
        if sty:
            styles.append(sty)
            text_nodes.append(extra_text + text)
            extra_text = ""
        else:
            extra_text += text
        last_end = mobj.end()
    text_nodes.append(extra_text + string[last_end:])
    first_node, *text_nodes = text_nodes

    # parse 
    style_stack = [FormatTree(Style("base", True), []), FormatTree(Style("para", {}), [first_node])]
    for style, text in zip(styles, text_nodes):
        tmp_stylestack = []
        if style.type == "base":
            while style_stack[-1].style.type != "para":
                last_node = style_stack.pop()
                style_stack[-1].children.append(last_node)
        elif any(tree.style.type == style.type for tree in style_stack):
            while len(style_stack) >= 2:
                last_node = style_stack.pop()
                style_stack[-1].children.append(last_node)
                if last_node.style.type == style.type:
                    break
                tmp_stylestack.append(last_node.style)
        for sty in tmp_stylestack:
            style_stack.append(FormatTree(sty, []))
        if style.value is None:
            if text: style_stack[-1].children.append(text)
        else:
            style_stack.append(FormatTree(style, [text] if text else []))
    while len(style_stack) >= 2:
        last_node = style_stack.pop()
        style_stack[-1].children.append(last_node)

    return style_stack[0]

test_root = {"i18n": {}, "macros": default_macros, "resource_dir": "Common/src/main/resources", "modid": "hexcasting"}
test_str = "Write the given iota to my $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$().$(br)The $(l:patterns/readwrite#hexcasting:write/local)$(#490)local$() is a lot like a $(l:items/focus)$(#b0b)Focus$(). It's cleared when I stop casting a Hex, starts with $(l:casting/influences)$(#490)Null$() in it, and is preserved between casts of $(l:patterns/meta#hexcasting:for_each)$(#fc77be)Thoth's Gambit$(). "

def do_localize(root_data, obj, *names):
    for name in names:
        if name in obj:
            obj[name] = localize(root_data["i18n"], obj[name])

def do_format(root_data, obj, *names):
    for name in names:
        if name in obj:
            obj[name] = format_string(root_data, obj[name])

def identity(x): return x

pattern_pat = re.compile(r'HexPattern\.fromAngles\("([qweasd]+)", HexDir\.(\w+)\),\s*modLoc\("([^"]+)"\)([^;]*true\);)?')
pattern_stubs = [(None, "at/petrak/hexcasting/interop/pehkui/PehkuiInterop.java"), (None, "at/petrak/hexcasting/common/casting/RegisterPatterns.java"), ("Fabric", "at/petrak/hexcasting/fabric/interop/gravity/GravityApiInterop.java")]
def fetch_patterns(root_data):
    registry = {}
    for loader, stub in pattern_stubs:
        filename = f"{root_data['resource_dir']}/../java/{stub}"
        if loader: filename = filename.replace("Common", loader)
        with open(filename, "r") as fh:
            pattern_data = fh.read()
            for mobj in re.finditer(pattern_pat, pattern_data):
                string, start_angle, name, is_per_world = mobj.groups()
                registry[root_data["modid"] + ":" + name] = (string, start_angle, bool(is_per_world))
    return registry

def resolve_pattern(root_data, page):
    if "pattern_reg" not in root_data:
        root_data["pattern_reg"] = fetch_patterns(root_data)
    page["op"] = [root_data["pattern_reg"][page["op_id"]]]
    page["name"] = localize(root_data["i18n"], "hexcasting.spell." + page["op_id"])

def fixup_pattern(do_sig, root_data, page):
    patterns = page["patterns"]
    if not isinstance(patterns, list): patterns = [patterns]
    if do_sig:
        inp = page.get("input", None) or ""
        oup = page.get("output", None) or ""
        pipe = f"{inp} \u2192 {oup}".strip()
        suffix = f" ({pipe})" if inp or oup else ""
        page["header"] += suffix
    page["op"] = [(p["signature"], p["startdir"], False) for p in patterns]

def fetch_recipe(root_data, recipe):
    modid, recipeid = recipe.split(":")
    gen_resource_dir = root_data["resource_dir"].replace("/main/", "/generated/").replace("Common/", "Forge/") # TODO hack
    recipe_path = f"{gen_resource_dir}/data/{modid}/recipes/{recipeid}.json"
    return slurp(recipe_path)
def fetch_recipe_result(root_data, recipe):
    return fetch_recipe(root_data, recipe)["result"]["item"]
def fetch_bswp_recipe_result(root_data, recipe):
    return fetch_recipe(root_data, recipe)["result"]["name"]

def localize_item(root_data, item):
    # TODO hack
    item = re.sub("{.*", "", item.replace(":", "."))
    block = "block." + item
    block_l = localize(root_data["i18n"], block)
    if block_l != block: return block_l
    return localize(root_data["i18n"], "item." + item)

page_types = {
    "hexcasting:pattern": resolve_pattern,
    "hexcasting:manual_pattern": bind1(fixup_pattern, True),
    "hexcasting:manual_pattern_nosig": bind1(fixup_pattern, False),
    "hexcasting:brainsweep": lambda rd, page: page.__setitem__("output_name", localize_item(rd, fetch_bswp_recipe_result(rd, page["recipe"]))),
    "patchouli:link": lambda rd, page: do_localize(rd, page, "link_text"),
    "patchouli:crafting": lambda rd, page: page.__setitem__("item_name", [localize_item(rd, fetch_recipe_result(rd, page[ty])) for ty in ("recipe", "recipe2") if ty in page]),
    "hexcasting:crafting_multi": lambda rd, page: page.__setitem__("item_name", [localize_item(rd, fetch_recipe_result(rd, recipe)) for recipe in page["recipes"]]),
    "patchouli:spotlight": lambda rd, page: page.__setitem__("item_name", localize_item(rd, page["item"]))
}

def walk_dir(root_dir, prefix):
    search_dir = root_dir + '/' + prefix
    for fh in os.scandir(search_dir):
        if fh.is_dir():
            yield from walk_dir(root_dir, prefix + fh.name + '/')
        elif fh.name.endswith(".json"):
            yield prefix + fh.name

def parse_entry(root_data, entry_path, ent_name):
    data = slurp(f"{entry_path}")
    do_localize(root_data, data, "name")
    for i, page in enumerate(data["pages"]):
        if isinstance(page, str):
            page = {"type": "patchouli:text", "text": page}
            data["pages"][i] = page
            
        do_localize(root_data, page, "title", "header")
        do_format(root_data, page, "text")
        if page["type"] in page_types:
            page_types[page["type"]](root_data, page)
    data["id"] = ent_name

    return data

def parse_category(root_data, base_dir, cat_name):
    data = slurp(f"{base_dir}/categories/{cat_name}.json")
    do_localize(root_data, data, "name")
    do_format(root_data, data, "description")

    entry_dir = f"{base_dir}/entries/{cat_name}"
    entries = []
    for filename in os.listdir(entry_dir):
        if filename.endswith(".json"):
            basename = filename[:-5]
            entries.append(parse_entry(root_data, f"{entry_dir}/{filename}", cat_name + "/" + basename))
    entries.sort(key=lambda ent: (not ent.get("priority", False), ent.get("sortnum", 0), ent["name"]))
    data["entries"] = entries
    data["id"] = cat_name

    return data

def parse_sortnum(cats, name):
    if '/' in name:
        ix = name.rindex('/')
        return parse_sortnum(cats, name[:ix]) + (cats[name].get("sortnum", 0),)
    return cats[name].get("sortnum", 0),

def parse_book(root, mod_name, book_name):
    base_dir = f"{root}/data/{mod_name}/patchouli_books/{book_name}"
    root_info = slurp(f"{base_dir}/book.json")

    root_info["resource_dir"] = root
    root_info["modid"] = mod_name
    root_info.setdefault("macros", {}).update(default_macros)
    if root_info.setdefault("i18n", {}):
        root_info["i18n"] = slurp(f"{root}/assets/{mod_name}/lang/{lang}.json")
        root_info["i18n"].update(extra_i18n)

    book_dir = f"{base_dir}/{lang}"

    categories = []
    for filename in walk_dir(f"{book_dir}/categories", ""):
        basename = filename[:-5]
        categories.append(parse_category(root_info, book_dir, basename))
    cats = {cat["id"]: cat for cat in categories}
    categories.sort(key=lambda cat: (parse_sortnum(cats, cat["id"]), cat["name"]))

    do_localize(root_info, root_info, "name")
    do_format(root_info, root_info, "landing_text")
    root_info["categories"] = categories
    root_info["blacklist"] = set()
    root_info["spoilers"] = set()

    return root_info

def tag_args(kwargs):
    return "".join(f" {'class' if key == 'clazz' else key.replace('_', '-')}={repr(value)}" for key, value in kwargs.items())

class PairTag:
    __slots__ = ["stream", "name", "kwargs"]
    def __init__(self, stream, name, **kwargs):
        self.stream = stream
        self.name = name
        self.kwargs = tag_args(kwargs)
    def __enter__(self):
        print(f"<{self.name}{self.kwargs}>", file=self.stream, end="")
    def __exit__(self, _1, _2, _3):
        print(f"</{self.name}>", file=self.stream, end="")

class Empty:
    def __enter__(self): pass
    def __exit__(self, _1, _2, _3): pass

class Stream:
    __slots__ = ["stream", "thunks"]
    def __init__(self, stream):
        self.stream = stream
        self.thunks = []

    def tag(self, name, **kwargs):
        keywords = tag_args(kwargs)
        print(f"<{name}{keywords} />", file=self.stream, end="")
        return self

    def pair_tag(self, name, **kwargs):
        return PairTag(self.stream, name, **kwargs)

    def pair_tag_if(self, cond, name, **kwargs):
        return self.pair_tag(name, **kwargs) if cond else Empty()

    def empty_pair_tag(self, name, **kwargs):
        with self.pair_tag(name, **kwargs): pass

    def text(self, txt):
        print(txt, file=self.stream, end="")
        return self

def get_format(out, ty, value):
    if ty == "para":
        return out.pair_tag("p", **value)
    if ty == "color":
        return out.pair_tag("span", style=f"color: #{value}")
    if ty == "link":
        link = value
        if "://" not in link:
            link = "#" + link.replace("#", "@")
        return out.pair_tag("a", href=link)
    if ty == "tooltip":
        return out.pair_tag("span", clazz="has-tooltip", title=value)
    if ty == "cmd_click":
        return out.pair_tag("span", clazz="has-cmd_click", title="When clicked, would execute: "+value)
    if ty == "obf":
        return out.pair_tag("span", clazz="obfuscated")
    if ty == "bold":
        return out.pair_tag("strong")
    if ty == "italic":
        return out.pair_tag("i")
    if ty == "strikethrough":
        return out.pair_tag("s")
    if ty == "underline":
        return out.pair_tag("span", style="text-decoration: underline")
    raise ValueError("Unknown format type: " + ty)

def entry_spoilered(root_info, entry):
    return entry.get("advancement", None) in root_info["spoilers"]

def category_spoilered(root_info, category):
    return all(entry_spoilered(root_info, ent) for ent in category["entries"])

def write_block(out, block):
    if isinstance(block, str):
        out.text(block)
        return
    sty_type = block.style.type
    if sty_type == "base":
        for child in block.children: write_block(out, child)
        return
    tag = get_format(out, sty_type, block.style.value)
    with tag:
        for child in block.children:
            write_block(out, child)

# TODO modularize
def write_page(out, pageid, page):
    if "anchor" in page:
        anchor_id = pageid + "@" + page["anchor"]
    else: anchor_id = None

    with out.pair_tag_if(anchor_id, "div", id=anchor_id):
        if "header" in page or "title" in page:
            with out.pair_tag("h4"):
                out.text(page.get("header", page.get("title", None)))
                if anchor_id:
                    with out.pair_tag("a", href="#" + anchor_id, clazz="permalink small"):
                        out.empty_pair_tag("i", clazz="bi bi-link-45deg")

        ty = page["type"]
        if ty == "patchouli:text":
            write_block(out, page["text"])
        elif ty == "patchouli:empty": pass
        elif ty == "patchouli:link":
            write_block(out, page["text"])
            with out.pair_tag("h4", clazz="linkout"):
                with out.pair_tag("a", href=page["url"]):
                    out.text(page["link_text"])
        elif ty == "patchouli:spotlight":
            with out.pair_tag("h4", clazz="spotlight-title page-header"):
                out.text(page["item_name"])
            if "text" in page: write_block(out, page["text"])
        elif ty == "patchouli:crafting":
            with out.pair_tag("blockquote", clazz="crafting-info"):
                out.text(f"Depicted in the book: The crafting recipe for the ")
                first = True
                for name in page["item_name"]:
                    if not first: out.text(" and ")
                    first = False
                    with out.pair_tag("code"): out.text(name)
                out.text(".")
            if "text" in page: write_block(out, page["text"])
        elif ty == "patchouli:image":
            with out.pair_tag("p", clazz="img-wrapper"):
                for img in page["images"]:
                    modid, coords = img.split(":")
                    out.empty_pair_tag("img", src=f"{repo_names[modid]}/assets/{modid}/{coords}")
            if "text" in page: write_block(out, page["text"])
        elif ty == "hexcasting:crafting_multi":
            recipes = page["item_name"]
            with out.pair_tag("blockquote", clazz="crafting-info"):
                out.text(f"Depicted in the book: Several crafting recipes, for the ")
                with out.pair_tag("code"): out.text(recipes[0])
                for i in recipes[1:]:
                    out.text(", ")
                    with out.pair_tag("code"): out.text(i)
                out.text(".")
            if "text" in page: write_block(out, page["text"])
        elif ty == "hexcasting:brainsweep": 
            with out.pair_tag("blockquote", clazz="crafting-info"):
                out.text(f"Depicted in the book: A mind-flaying recipe producing the ")
                with out.pair_tag("code"): out.text(page["output_name"])
                out.text(".")
            if "text" in page: write_block(out, page["text"])
        elif ty in ("hexcasting:pattern", "hexcasting:manual_pattern_nosig", "hexcasting:manual_pattern"):
            if "name" in page:
                with out.pair_tag("h4", clazz="pattern-title"):
                    inp = page.get("input", None) or ""
                    oup = page.get("output", None) or ""
                    pipe = f"{inp} \u2192 {oup}".strip()
                    suffix = f" ({pipe})" if inp or oup else ""
                    out.text(f"{page['name']}{suffix}")
                    if anchor_id:
                        with out.pair_tag("a", href="#" + anchor_id, clazz="permalink small"):
                            out.empty_pair_tag("i", clazz="bi bi-link-45deg")
            with out.pair_tag("details", clazz="spell-collapsible"):
                out.empty_pair_tag("summary", clazz="collapse-spell")
                for string, start_angle, per_world in page["op"]:
                    with out.pair_tag("canvas", clazz="spell-viz", width=216, height=216, data_string=string, data_start=start_angle.lower(), data_per_world=per_world):
                        out.text("Your browser does not support visualizing patterns. Pattern code: " + string)
            write_block(out, page["text"])
        else:
            with out.pair_tag("p", clazz="todo-note"):
                out.text("TODO: Missing processor for type: " + ty)
            if "text" in page:
                write_block(out, page["text"])
    out.tag("br")

def write_entry(out, book, entry):
    with out.pair_tag("div", id=entry["id"]):
        with out.pair_tag_if(entry_spoilered(book, entry), "div", clazz="spoilered"):
            with out.pair_tag("h3", clazz="entry-title page-header"):
                write_block(out, entry["name"])
                with out.pair_tag("a", href="#" + entry["id"], clazz="permalink small"):
                    out.empty_pair_tag("i", clazz="bi bi-link-45deg")
            for page in entry["pages"]:
                write_page(out, entry["id"], page)

def write_category(out, book, category):
    with out.pair_tag("section", id=category["id"]):
        with out.pair_tag_if(category_spoilered(book, category), "div", clazz="spoilered"):
            with out.pair_tag("h2", clazz="category-title page-header"):
                write_block(out, category["name"])
                with out.pair_tag("a", href="#" + category["id"], clazz="permalink small"):
                    out.empty_pair_tag("i", clazz="bi bi-link-45deg")
            write_block(out, category["description"])
        for entry in category["entries"]:
            if entry["id"] not in book["blacklist"]:
                    write_entry(out, book, entry)

def write_toc(out, book):
    with out.pair_tag("h2", id="table-of-contents", clazz="page-header"):
        out.text("Table of Contents")
        with out.pair_tag("a", href="javascript:void(0)", clazz="toggle-link small", data_target="toc-category"):
            out.text("(toggle all)")
        with out.pair_tag("a", href="#table-of-contents", clazz="permalink small"):
            out.empty_pair_tag("i", clazz="bi bi-link-45deg")
    for category in book["categories"]:
        with out.pair_tag("details", clazz="toc-category"):
            with out.pair_tag("summary"):
                with out.pair_tag("a", href="#" + category["id"], clazz="spoilered" if category_spoilered(book, category) else ""):
                    out.text(category["name"])
            with out.pair_tag("ul"):
                for entry in category["entries"]:
                    with out.pair_tag("li"):
                        with out.pair_tag("a", href="#" + entry["id"], clazz="spoilered" if entry_spoilered(book, entry) else ""):
                            out.text(entry["name"])

def write_book(out, book):
    with out.pair_tag("div", clazz="container"):
        with out.pair_tag("header", clazz="jumbotron"):
            with out.pair_tag("h1", clazz="book-title"):
                write_block(out, book["name"])
            write_block(out, book["landing_text"])
        with out.pair_tag("nav"):
            write_toc(out, book)
        with out.pair_tag("main", clazz="book-body"):
            for category in book["categories"]:
                write_category(out, book, category)

def main(argv):
    if len(argv) < 5:
        print(f"Usage: {argv[0]} <resources dir> <mod name> <book name> <template file> [<output>]")
        return
    root = argv[1]
    mod_name = argv[2]
    book_name = argv[3]
    book = parse_book(root, mod_name, book_name)
    template_file = argv[4]
    with open(template_file, "r") as fh:
        with stdout if len(argv) < 6 else open(argv[5], "w") as out:
            for line in fh:
                if line.startswith("#DO_NOT_RENDER"):
                    _, *blacklist = line.split()
                    book["blacklist"].update(blacklist)
                if line.startswith("#SPOILER"):
                    _, *spoilers = line.split()
                    book["spoilers"].update(spoilers)
                elif line == "#DUMP_BODY_HERE\n":
                    write_book(Stream(out), book)
                    print('', file=out)
                else: print(line, end='', file=out)

if __name__ == "__main__":
    main(argv)
