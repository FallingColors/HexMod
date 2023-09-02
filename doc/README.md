# doc

Contains the Python docgen for Hex Casting.

## Version scheme

This package uses [hatch-gradle-version](https://pypi.org/project/hatch-gradle-version) to generate a version number based on the mod version it was built with.

The version is in this format: `mod-version.python-version.mod-pre.python-dev.python-post`

For example:
* Mod version: `0.11.1-7`
* Python package version: `1.0.dev0`
* Full version: `0.11.1.1.0rc7.dev0`

## Setup

```sh
python -m venv venv

.\venv\Scripts\activate  # Windows
source venv/bin/activate # anything other than Windows

# run from the repo root, not doc/
pip install -e .[dev]
```

### CI/CD

- Under Settings > Environments, create a new environment called `pypi`
- Follow these instructions: https://docs.pypi.org/trusted-publishers/creating-a-project-through-oidc/
- TODO

## Usage

```sh
hexdoc doc/properties.toml -o out
```
