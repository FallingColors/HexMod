# {{ cookiecutter.modid }}

This is the Python docgen for {{ cookiecutter.modid }}.

## Version scheme

We use [hatch-gradle-version](https://pypi.org/project/hatch-gradle-version) to generate the version number based on whichever mod version the docgen was built with.

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

WIP.

- Under Settings > Environments, create a new environment called `pypi` (and optionally `testpypi`)
- Follow these instructions: https://docs.pypi.org/trusted-publishers/creating-a-project-through-oidc/

## Usage

```sh
# run from the repo root, not doc/
hexdoc ./doc/properties.toml -o _site/src/docs
hexdoc_merge --src _site/src/docs --dst _site/dst/docs
```
