__all__ = [
    "hookimpl",
    "ModVersionImpl",
    "LoadResourceDirsImpl",
    "LoadTaggedUnionsImpl",
    "LoadJinjaTemplatesImpl",
    "UpdateJinjaEnvImpl",
    "UpdateTemplateArgsImpl",
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
    UpdateJinjaEnvImpl,
    UpdateTemplateArgsImpl,
)

hookimpl = pluggy.HookimplMarker(HEXDOC_PROJECT_NAME)
