# from pathlib import Path

# from common.properties import Properties
# from hexcasting.hex_book import HexBook


# # def test_serde():
# #     props = Properties.load(Path("properties.toml"))
# #     book = HexBook.load(*HexBook.prepare(props))

# #     serded_book = HexBook.model_validate_json(
# #         book.model_dump_json(
# #             round_trip=True,
# #             by_alias=True,
# #         )
# #     )

# #     assert serded_book == book
