from hexdoc.__gradle_version__ import GRADLE_VERSION
from hexdoc.plugin import hookimpl


@hookimpl
def hexdoc_mod_version():
    return GRADLE_VERSION
