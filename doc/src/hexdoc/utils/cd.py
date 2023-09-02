from pathlib import Path
from typing import Annotated

from pydantic import AfterValidator, ValidationInfo

from .deserialize import cast_or_raise
from .model import ValidationContext


class RelativePathContext(ValidationContext):
    root: Path


def validate_relative_path(path: Path, info: ValidationInfo):
    context = cast_or_raise(info.context, RelativePathContext)
    return context.root / path


RelativePath = Annotated[Path, AfterValidator(validate_relative_path)]
