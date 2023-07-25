# TODO: type
import io
from contextlib import nullcontext
from dataclasses import dataclass
from html import escape
from typing import IO, Any

from hexdoc.minecraft import LocalizedStr


def attributes_to_str(attributes: dict[str, Any]):
    return "".join(
        f" {'class' if key == 'class_name' else key.replace('_', '-')}={repr(escape(str(value)))}"
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


class HTMLStream(io.StringIO):
    def void_element(self, name: str, **kwargs: Any):
        """Like `<img />`."""
        keywords = attributes_to_str(kwargs)
        self.write(f"<{name}{keywords} />")

    def element(self, name: str, /, **kwargs: Any):
        return HTMLElement(self, name, kwargs)

    def element_if(self, condition: bool, name: str, /, **kwargs: Any):
        if condition:
            return self.element(name, **kwargs)
        return nullcontext()

    def empty_element(self, name: str, **kwargs: Any):
        with self.element(name, **kwargs):
            pass

    def text(self, txt: str | LocalizedStr):
        self.write(escape(str(txt)))
        return self
