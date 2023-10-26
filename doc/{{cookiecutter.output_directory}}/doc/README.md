# {{ cookiecutter.__project_name }}

Python web book docgen and [hexdoc](https://pypi.org/project/hexdoc) plugin for {{ cookiecutter.mod_display_name }}.

## Version scheme

We use [hatch-gradle-version](https://pypi.org/project/hatch-gradle-version) to generate the version number based on whichever mod version the docgen was built with.

The version is in this format: `mod-version.python-version.mod-pre.python-dev.python-post`

For example:
* Mod version: `0.11.1-7`
* Python package version: `1.0.dev0`
* Full version: `0.11.1.1.0rc7.dev0`

## Setup

```sh
python3.11 -m venv venv

.\venv\Scripts\activate   # Windows
. venv/bin/activate.fish  # fish
source venv/bin/activate  # everything else

# run from the repo root, not doc/
pip install -e .[dev]
```

## Usage

For local testing, create a file called `.env` in the repo root following this template:
```sh
GITHUB_REPOSITORY={{ cookiecutter.author }}/{{ cookiecutter.github_repo }}
GITHUB_SHA={{ cookiecutter.main_branch }}
GITHUB_PAGES_URL={{ cookiecutter.pages_url }}
```

Then run these commands to generate the book:
```sh
# run from the repo root, not doc/
hexdoc render
hexdoc merge
```

Or, run this command to render the book and start a local web server:
```sh
hexdoc serve --lang en_us
```
