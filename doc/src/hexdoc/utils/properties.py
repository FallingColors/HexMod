from __future__ import annotations

import re
from collections.abc import Iterator
from pathlib import Path
from typing import Annotated, Any, Literal, Self, TypeVar

from pydantic import AfterValidator, Field, HttpUrl
from typing_extensions import TypedDict

from .model import HexDocStripHiddenModel
from .resource import ResourceLocation
from .toml_placeholders import load_toml_with_placeholders

ResourceType = Literal["assets", "data"]

NoTrailingSlashHttpUrl = Annotated[
    str,
    HttpUrl,
    AfterValidator(lambda u: str(u).rstrip("/")),
]


class PatternStubProps(HexDocStripHiddenModel[Any]):
    path: Path
    regex: re.Pattern[str]


class XplatProps(HexDocStripHiddenModel[Any]):
    src: Path
    pattern_stubs: list[PatternStubProps] | None = None
    resources: Path


class PlatformProps(XplatProps):
    recipes: Path
    tags: Path


class I18nProps(HexDocStripHiddenModel[Any]):
    default_lang: str
    extra: dict[str, str] = Field(default_factory=dict)
    keys: dict[str, str] = Field(default_factory=dict)


class Properties(HexDocStripHiddenModel[Any]):
    modid: str
    book: ResourceLocation
    url: NoTrailingSlashHttpUrl

    is_0_black: bool
    """If true, the style `$(0)` changes the text color to black; otherwise it resets
    the text color to the default."""

    resource_dirs: list[Path]

    spoilered_advancements: set[ResourceLocation]
    entry_id_blacklist: set[ResourceLocation]

    template: str
    template_dirs: list[Path]
    template_packages: list[tuple[str, Path]]

    template_args: dict[str, Any]

    base_asset_urls: dict[str, NoTrailingSlashHttpUrl]
    """Mapping from modid to the url of that mod's `resources` directory on GitHub."""

    i18n: I18nProps

    pattern_stubs: list[PatternStubProps]

    @classmethod
    def load(cls, path: Path) -> Self:
        return cls.model_validate(load_toml_with_placeholders(path))

    def mod_loc(self, path: str) -> ResourceLocation:
        """Returns a ResourceLocation with self.modid as the namespace."""
        return ResourceLocation(self.modid, path)

    def get_asset_url(self, id: ResourceLocation) -> str:
        base_url = self.base_asset_urls[id.namespace]
        return f"{base_url}/{id.file_path_stub('assets').as_posix()}"

    def find_book_assets(self, folder: Literal["categories", "entries", "templates"]):
        return self.find_resources(
            type="assets",
            folder="patchouli_books",
            base_id=self.book / self.i18n.default_lang / folder,
        )

    def find_resource(
        self,
        type: ResourceType,
        folder: str,
        id: ResourceLocation,
    ) -> Path:
        """Find the first file with this resource location in `resource_dirs`.

        If no file extension is provided, `.json` is assumed.

        Raises FileNotFoundError if the file does not exist.
        """

        # check in each directory, return the first that exists
        path_stub = id.file_path_stub(type, folder)
        for resource_dir in self.resource_dirs:
            path = resource_dir / path_stub
            if path.is_file():
                return path

        raise FileNotFoundError(f"Path {path_stub} not found in any resource dir")

    def find_resources(
        self,
        type: ResourceType,
        folder: str,
        base_id: ResourceLocation,
        glob: str | list[str] = "**/*",
        reverse: bool = True,
    ) -> Iterator[tuple[ResourceLocation, Path]]:
        """Search for a glob under a given resource location in all of `resource_dirs`.

        The path of the returned resource location is relative to the path of base_id.

        If no file extension is provided for glob, `.json` is assumed.

        Raises FileNotFoundError if no files were found in any resource dir.

        For example:
        ```py
        props.find_resources(
            type="assets",
            folder="lang",
            base_id=ResLoc("*", "subdir"),
            glob="*.flatten.json5",
        )

        # [(hexcasting:en_us, .../resources/assets/hexcasting/lang/subdir/en_us.json)]
        ```
        """

        # eg. assets/*/lang/subdir
        base_path_stub = base_id.file_path_stub(type, folder, assume_json=False)

        # glob for json files if not provided
        globs = [glob] if isinstance(glob, str) else glob
        for i in range(len(globs)):
            if not Path(globs[i]).suffix:
                globs[i] += ".json"

        # find all files matching the resloc
        found_any = False
        for resource_dir in (
            reversed(self.resource_dirs) if reverse else self.resource_dirs
        ):
            # eg. .../resources/assets/*/lang/subdir
            for base_path in resource_dir.glob(base_path_stub.as_posix()):
                for glob_ in globs:
                    # eg. .../resources/assets/hexcasting/lang/subdir/*.flatten.json5
                    for path in base_path.glob(glob_):
                        # only yield actual files
                        if not path.is_file():
                            continue
                        found_any = True

                        # determine the resource location of this file
                        # eg. en_us.flatten.json5 -> hexcasting:en_us
                        path_stub = path.relative_to(base_path)
                        while path_stub.suffix:
                            path_stub = path_stub.with_suffix("")
                        id = ResourceLocation(base_id.namespace, path_stub.as_posix())

                        yield id, path

        # if we never yielded any files, raise an error
        if not found_any:
            raise FileNotFoundError(
                f"No files found under {base_path_stub}/{globs} in any resource dir"
            )


class PropsContext(TypedDict):
    props: Properties


AnyPropsContext = TypeVar("AnyPropsContext", bound=PropsContext)
