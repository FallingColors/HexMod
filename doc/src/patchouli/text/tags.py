# TODO: type
from contextlib import nullcontext
from dataclasses import InitVar, dataclass
from html import escape
from typing import IO, Any

from minecraft.i18n import LocalizedStr


def tag_args(kwargs: dict[str, Any]):
    return "".join(
        f" {'class' if key == 'clazz' else key.replace('_', '-')}={repr(escape(str(value)))}"
        for key, value in kwargs.items()
    )


@dataclass
class PairTag:
    stream: IO[str]
    name: str
    args: InitVar[dict[str, Any]]

    def __post_init__(self, args: dict[str, Any]):
        self.args_str = tag_args(args)

    def __enter__(self):
        # TODO: self.stream.write??????????
        print(f"<{self.name}{self.args_str}>", file=self.stream, end="")

    def __exit__(self, _1: Any, _2: Any, _3: Any):
        print(f"</{self.name}>", file=self.stream, end="")


class Empty:
    def __enter__(self):
        pass

    def __exit__(self, _1: Any, _2: Any, _3: Any):
        pass


class Stream:
    __slots__ = ["stream"]

    def __init__(self, stream: IO[str]):
        self.stream = stream

    def tag(self, name: str, **kwargs: Any):
        keywords = tag_args(kwargs)
        print(f"<{name}{keywords} />", file=self.stream, end="")
        return self

    def pair_tag(self, name: str, **kwargs: Any):
        return PairTag(self.stream, name, kwargs)

    def pair_tag_if(self, cond: Any, name: str, **kwargs: Any):
        return self.pair_tag(name, **kwargs) if cond else Empty()

    def empty_pair_tag(self, name: str, **kwargs: Any):
        with self.pair_tag(name, **kwargs):
            pass

    def null_tag(self):
        return nullcontext()

    def text(self, txt: str | LocalizedStr):
        print(escape(str(txt)), file=self.stream, end="")
        return self
