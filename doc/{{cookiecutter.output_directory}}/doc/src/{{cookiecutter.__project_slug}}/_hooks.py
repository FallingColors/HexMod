from importlib.resources import Package

from hexdoc.plugin import LoadResourceDirsImpl, ModVersionImpl, hookimpl

from .__gradle_version__ import GRADLE_VERSION


class {{ cookiecutter.plugin_classname }}(LoadResourceDirsImpl, ModVersionImpl):
    @staticmethod
    @hookimpl
    def hexdoc_mod_version() -> str:
        return GRADLE_VERSION

    @staticmethod
    @hookimpl
    def hexdoc_load_resource_dirs() -> Package | list[Package]:
        # This needs to be a lazy import because they may not exist when this file is
        # first loaded, eg. when generating the contents of generated.
        from ._export import generated, resources

        return [generated, resources]
