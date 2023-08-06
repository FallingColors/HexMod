import os
from contextlib import contextmanager
from typing import AnyStr


# https://stackoverflow.com/a/24176022
@contextmanager
def cd(newdir: os.PathLike[AnyStr]):
    """Context manager which temporarily changes the script's working directory."""
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)
