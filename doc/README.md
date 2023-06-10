# doc

Contains the Python docgen for Hex Casting.

## Setup

The minimum Python version to run this script is `3.11`.

```sh
cd doc
python -m venv venv

.\venv\Scripts\activate  # Windows
source venv/bin/activate # anything other than Windows

pip install -r requirements.txt
```

## Usage

```sh
python src/main.py ../Common/src/main/resources hexcasting thehexbook template.html out.html
```
