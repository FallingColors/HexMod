from pluggy import HookspecMarker

hookspec = HookspecMarker("hexdoc")


@hookspec(firstresult=True)
def hexdoc_mod_version():
    """Return the mod version (aka `GRADLE_VERSION`) from `__gradle_version__.py`."""
