import logging


def setup_logging(verbosity: int):
    logging.basicConfig(
        style="{",
        format="\033[1m[{relativeCreated:.02f} | {levelname} | {name}]\033[0m {message}",
        level=log_level(verbosity),
    )
    logging.getLogger(__name__).info("Starting.")


def log_level(verbosity: int) -> int:
    match verbosity:
        case 0:
            return logging.WARNING
        case 1:
            return logging.INFO
        case _:
            return logging.DEBUG
