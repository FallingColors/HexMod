# hexdoc

This is the Python docgen for Hex Casting.

## Version scheme

We use [hatch-gradle-version](https://pypi.org/project/hatch-gradle-version) to generate the version number based on whichever mod version the docgen was built with.

The version is in this format: `mod-version.python-version.mod-pre.python-dev.python-post`

For example:
* Mod version: `0.11.1-7`
* Python package version: `1.0.dev0`
* Full version: `0.11.1.1.0rc7.dev0`

## Creating a plugin / addon

WIP.

- Run these commands, then follow the prompts:
  ```sh
  pip install cookiecutter
  cookiecutter gh:object-Object/HexMod --directory doc
  ```
  - Note: if you run this from within an existing mod repo, add the flag `-f` and leave `output_directory` blank.
- Fill in the TODOs in `doc/properties.toml` (mostly paths to files/folders in your mod so hexdoc can find the data it needs).
- Try running the docgen locally by following the instructions in `doc/README.md`.
- If it doesn't already exist, create an empty `gh-pages` branch and push it.
- On GitHub, under `Settings > Pages`, set the source to `Deploy from a branch`, the branch to `gh-pages`, and the folder to `docs/`.
- Commit and push the docgen, and see if the CI works.
- On GitHub, under `Settings > Environments`, create two new environments called `pypi` and `testpypi`.
- Follow these instructions for PyPI and TestPyPI: https://docs.pypi.org/trusted-publishers/creating-a-project-through-oidc/
  - TestPyPI is a duplicate of PyPI which can be used for testing package publishing without affecting the real index. The CI workflow includes a manual execution option to publish to TestPyPI.
  - If you like to live dangerously, this step is optional - you can remove the `publish-testpypi` job and the `TestPyPI` release choice from your workflow without impacting the rest of the CI.

## Setup

```sh
python -m venv venv

.\venv\Scripts\activate  # Windows
source venv/bin/activate # anything other than Windows

# run from the repo root, not doc/
pip install -e .[dev]
```

## Usage

```sh
# run from the repo root, not doc/
hexdoc ./doc/properties.toml -o _site/src/docs
hexdoc_merge --src _site/src/docs --dst _site/dst/docs
```
