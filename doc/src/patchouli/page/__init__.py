from common.deserialize import TypeHooks
from common.tagged_union import get_union_types

from .abstract import *
from .hexcasting import *
from .patchouli import *

Page = (
    TextPage
    | ImagePage
    | CraftingPage
    | SmeltingPage
    | MultiblockPage
    | EntityPage
    | SpotlightPage
    | LinkPage
    | RelationsPage
    | QuestPage
    | EmptyPage
    | PatternPage
    | ManualPatternNosigPage
    | ManualOpPatternPage
    | ManualRawPatternPage
    | CraftingMultiPage
    | BrainsweepPage
)


def _raw_page_hook(data: dict[str, Any] | str) -> dict[str, Any]:
    if isinstance(data, str):
        # special case, thanks patchouli
        return {"type": "patchouli:text", "text": data}
    return data


def make_page_hooks(book: Book) -> TypeHooks:
    """Creates type hooks for deserializing Page types."""

    type_hooks: TypeHooks = {Page: _raw_page_hook}

    for cls_ in get_union_types(Page):
        type_hooks[cls_] = cls_.make_type_hook(book)

    return type_hooks
