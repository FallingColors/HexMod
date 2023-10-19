from importlib.resources import Package

from hexdoc.plugin import (
    HookReturn,
    LoadJinjaTemplatesImpl,
    LoadResourceDirsImpl,
    ModVersionImpl,
    hookimpl,
)

import {{ cookiecutter.__project_slug }}

from .__gradle_version__ import GRADLE_VERSION


class {{ cookiecutter.plugin_classname }}(
    LoadJinjaTemplatesImpl,
    LoadResourceDirsImpl,
    ModVersionImpl,
):
    @staticmethod
    @hookimpl
    def hexdoc_mod_version() -> str:
        return GRADLE_VERSION

    @staticmethod
    @hookimpl
    def hexdoc_load_resource_dirs() -> HookReturn[Package]:
        # This needs to be a lazy import because they may not exist when this file is
        # first loaded, eg. when generating the contents of generated.
        from ._export import generated, resources

        return [generated, resources]
    
    @staticmethod
    @hookimpl
    def hexdoc_load_jinja_templates() -> HookReturn[tuple[Package, str]]:
        return {{ cookiecutter.__project_slug }}, "_templates"
