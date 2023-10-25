# pyright: reportUnknownMemberType=false, reportUnknownVariableType=false

from typing import Any, Callable

import pytest
from jinja2.sandbox import SandboxedEnvironment
from markupsafe import Markup
from pytest import FixtureRequest, Mark

from hexdoc.cli.utils.render import create_jinja_env
from hexdoc.plugin import (
    PluginManager,
    UpdateJinjaEnvImpl,
    UpdateTemplateArgsImpl,
    hookimpl,
)

RenderTemplate = Callable[[], str]


@pytest.fixture
def render_template(request: FixtureRequest, pm: PluginManager) -> RenderTemplate:
    match request.node.get_closest_marker("template"):
        case Mark(args=[str(template_str)], kwargs=template_args):
            pass
        case marker:
            raise TypeError(f"Expected marker `template` with 1 string, got {marker}")

    def callback():
        env = create_jinja_env(pm, [])
        template = env.from_string(template_str)
        return template.render(pm.update_template_args(dict(template_args)))

    return callback


@pytest.mark.template("{{ '<br />' }}")
def test_update_jinja_env(pm: PluginManager, render_template: RenderTemplate):
    class Hooks(UpdateJinjaEnvImpl):
        @staticmethod
        @hookimpl
        def hexdoc_update_jinja_env(env: SandboxedEnvironment) -> None:
            env.autoescape = False

    assert render_template() == Markup.escape("<br />")
    pm.inner.register(Hooks)
    assert render_template() == "<br />"


@pytest.mark.template(
    "{{ key }}",
    key="old_value",
)
def test_update_template_args(pm: PluginManager, render_template: RenderTemplate):
    class Hooks(UpdateTemplateArgsImpl):
        @staticmethod
        @hookimpl
        def hexdoc_update_template_args(template_args: dict[str, Any]) -> None:
            template_args["key"] = "new_value"

    assert render_template() == "old_value"
    pm.inner.register(Hooks)
    assert render_template() == "new_value"
