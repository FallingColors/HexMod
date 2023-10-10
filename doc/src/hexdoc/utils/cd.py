from contextvars import ContextVar
from pathlib import Path
from typing import Annotated

from pydantic import AfterValidator

from .contextmanagers import set_contextvar

_relative_path_root = ContextVar[Path]("_relative_path_root")


def relative_path_root(path: Path):
    return set_contextvar(_relative_path_root, path)


RelativePath = Annotated[
    Path,
    AfterValidator(lambda path: _relative_path_root.get() / path),
]
