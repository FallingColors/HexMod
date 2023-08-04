from pytest import Parser


# https://stackoverflow.com/a/43938191
def pytest_addoption(parser: Parser):
    parser.addoption(
        "--longrun",
        action="store_true",
        dest="longrun",
        default=False,
        help="enable longrundecorated tests",
    )
