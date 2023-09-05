from pluggy import PluginManager


def name_hook_caller(pm: PluginManager, method_name: str, plugin_name: str):
    return pm.subset_hook_caller(
        name=method_name,
        remove_plugins=(
            plugin for name, plugin in pm.list_name_plugin() if name != plugin_name
        ),
    )
