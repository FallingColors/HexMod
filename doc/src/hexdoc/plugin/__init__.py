__all__ = [
    "hookimpl",
    "ModVersionImpl",
    "LoadResourceDirsImpl",
    "LoadTaggedUnionsImpl",
    "PluginManager",
]

import pluggy

from .manager import PluginManager
from .specs import (
    HEXDOC_PROJECT_NAME,
    LoadResourceDirsImpl,
    LoadTaggedUnionsImpl,
    ModVersionImpl,
)

hookimpl = pluggy.HookimplMarker(HEXDOC_PROJECT_NAME)
