__all__ = [
    "hookimpl",
    "ModVersionImpl",
    "LoadResourceDirsImpl",
    "LoadTaggedUnionsImpl",
    "LoadJinjaTemplatesImpl",
    "PluginManager",
    "HookReturn",
]

import pluggy

from .manager import PluginManager
from .specs import (
    HEXDOC_PROJECT_NAME,
    HookReturn,
    LoadJinjaTemplatesImpl,
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    ModVersionImpl,
)

hookimpl = pluggy.HookimplMarker(HEXDOC_PROJECT_NAME)
