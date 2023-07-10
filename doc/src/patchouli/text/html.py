# TODO: type
from contextlib import nullcontext
from dataclasses import dataclass
from html import escape
from typing import IO, Any

from minecraft.i18n import LocalizedStr


def attributes_to_str(attributes: dict[str, Any]):
    return "".join(
        f" {'class' if key == 'clazz' else key.replace('_', '-')}={repr(escape(str(value)))}"
        for key, value in attributes.items()
    )


@dataclass
class HTMLElement:
    stream: IO[str]
    name: str
    attributes: dict[str, Any]

    def __enter__(self) -> None:
        self.stream.write(f"<{self.name}{attributes_to_str(self.attributes)}>")

    def __exit__(self, *_: Any) -> None:
        self.stream.write(f"</{self.name}>")


@dataclass
class HTMLStream:
    stream: IO[str]

    def self_closing_element(self, name: str, **kwargs: Any):
        keywords = attributes_to_str(kwargs)
        self.stream.write(f"<{name}{keywords} />")
        return self

    def element(self, name: str, **kwargs: Any):
        return HTMLElement(self.stream, name, kwargs)

    def element_if(self, cond: Any, name: str, **kwargs: Any):
        return self.element(name, **kwargs) if cond else nullcontext()

    def empty_element(self, name: str, **kwargs: Any):
        with self.element(name, **kwargs):
            pass

    def text(self, txt: str | LocalizedStr):
        self.stream.write(escape(str(txt)))
        return self
