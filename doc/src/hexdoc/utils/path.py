import logging
from pathlib import Path


def strip_suffixes(path: Path) -> Path:
    """Removes all suffixes from a path. This is helpful because `path.with_suffix("")`
    only removes the last suffix.

    For example:
    ```py
    path = Path("lang/en_us.flatten.json5")
    strip_suffixes(path)  # lang/en_us
    path.with_suffix("")  # lang/en_us.flatten
    ```
    """
    while path.suffix:
        path = path.with_suffix("")
    return path


def replace_suffixes(path: Path, suffix: str) -> Path:
    """Replaces all suffixes of a path. This is helpful because `path.with_suffix()`
    only replaces the last suffix.

    For example:
    ```py
    path = Path("lang/en_us.flatten.json5")
    replace_suffixes(path, ".json")  # lang/en_us.json
    path.with_suffix(".json")        # lang/en_us.flatten.json
    ```
    """
    return strip_suffixes(path).with_suffix(suffix)


def write_to_path(path: Path, data: str | bytes, encoding: str = "utf-8"):
    logging.getLogger(__name__).debug(f"Writing to {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    match data:
        case str():
            path.write_text(data, encoding)
        case bytes():
            path.write_bytes(data)
