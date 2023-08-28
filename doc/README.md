# doc

Contains the Python docgen for Hex Casting.

## Setup

The minimum Python version to run this project is `3.11`.

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
