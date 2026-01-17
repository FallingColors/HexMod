# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [UNRELEASED]

### Added

- Added the `cannot_modify_cost` tag for patterns that should ignore the `media_consumption` attribute when calculating cost, by Robotgiggle in [987](https://github.com/FallingColors/HexMod/pull/987)

### Changed

- Changed the `media_consumption` attribute to only apply to player-based casting, by Robotgiggle in [987](https://github.com/FallingColors/HexMod/pull/987)

### Fixed

- Fixed a crash loop when trying to generate a creative-mode ancient scroll for a Great Spell whose per-world pattern hasn't been calculated yet, by Robotgiggle in [992](https://github.com/FallingColors/HexMod/pull/992).

## `0.11.3` - 2025-11-22

### Added

- Added several new player attributes (ported from [Hexxy Attributes](https://modrinth.com/mod/hexxyattributes)), by beholderface in [#823](https://github.com/FallingColors/HexMod/pull/823).
- Added ancient cyphers, by Robotgiggle in [#838](https://github.com/FallingColors/HexMod/pull/838).
- Added the ability for wandering traders to sell ancient scrolls, by Robotgiggle in [#837](https://github.com/FallingColors/HexMod/pull/837).
- Added a creative tab for ancient scrolls, by Robotgiggle in [#837](https://github.com/FallingColors/HexMod/pull/837).
- Added a config option to disable Greater Teleport item splatting, by TheDrawingCoding-Gamer in [#841](https://github.com/FallingColors/HexMod/pull/841).
- Added loot tables to generate exactly one random scroll/cypher, by Robotgiggle in [#844](https://github.com/FallingColors/HexMod/pull/844).
- Added recipes for slate, amethyst, and quenched allay blocks, by KyanBirb in [#903](https://github.com/FallingColors/HexMod/pull/903).
- hexdoc: Added the ability to load patterns from a JSON file (instead of scraping them from source code with regex), by object-Object in [#911](https://github.com/FallingColors/HexMod/pull/911).
- Added a new Ancient Pigment and changed the appearance of the default pigment, by Robotgiggle in [#912](https://github.com/FallingColors/HexMod/pull/912).

### Changed

- Updated the spell circle documentation to clarify their new behavior in 0.11.x, by Robotgiggle in [#814](https://github.com/FallingColors/HexMod/pull/814).
- Added the recipes for bamboo and cherry staves to the notebook entry, by Robotgiggle in [#814](https://github.com/FallingColors/HexMod/pull/814).
- Renamed the eval limit mishap from "Delve Too Deep" to "Lost in Thought", and updated the description and error message to match, by Robotgiggle in [#814](https://github.com/FallingColors/HexMod/pull/814).
- Updated zh_cn translations, by ChuijkYahus in [#799](https://github.com/FallingColors/HexMod/pull/799), [#828](https://github.com/FallingColors/HexMod/pull/828), [#847](https://github.com/FallingColors/HexMod/pull/847), [#913](https://github.com/FallingColors/HexMod/pull/913), [#953](https://github.com/FallingColors/HexMod/pull/953), [#954](https://github.com/FallingColors/HexMod/pull/954), and [#971](https://github.com/FallingColors/HexMod/pull/971).
- Added apostrophes to Compass' Purification in the notebook, by kineticneticat in [#867](https://github.com/FallingColors/HexMod/pull/867).
- Updated the description of Ignite to clarify that it works on all entities, by Robotgiggle in [#844](https://github.com/FallingColors/HexMod/pull/844).
- Improved documentation of iota embedding, by Robotgiggle in [#862](https://github.com/FallingColors/HexMod/pull/862).
- Documented the behaviour of the Shepherd Directrix when no boolean is present on the stack, by Robotgiggle in [#862](https://github.com/FallingColors/HexMod/pull/862).
- Changed vectors in the notebook to use parentheses rather than square brackets, by Robotgiggle in [#862](https://github.com/FallingColors/HexMod/pull/862).
- Changed the rarity of several post-enlightenment items, by Robotgiggle in [#860](https://github.com/FallingColors/HexMod/pull/860).
- API: Changed the base class of `Mishap` from `Throwable` to `RuntimeException` to reduce the likelihood of server crashes, by navarchus in [#933](https://github.com/FallingColors/HexMod/pull/933).
- API: Added `@Throws` annotation to several `Action` methods to allow Java pattern implementations to throw mishaps, by navarchus in [#935](https://github.com/FallingColors/HexMod/pull/935).
- Improved the documentation for the media cube, by object-Object in [#843](https://github.com/FallingColors/HexMod/pull/843).
- Changed Greater Teleport to mishap when a passenger is immune to teleportation, by Robotgiggle in [#916](https://github.com/FallingColors/HexMod/pull/916).
- Re-added the slate limit for spell circles, by Stickia in [#909](https://github.com/FallingColors/HexMod/pull/909).
- Renamed Inverse Tangent Purification II to Inverse Tangent Distillation, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Massively improved ru_ru translations, by JustS-js and LedinecMing in [#832](https://github.com/FallingColors/HexMod/pull/832).
- Changed the invalid-pattern mishap to display the offending pattern, by Robotgiggle in [#951](https://github.com/FallingColors/HexMod/pull/951).
- Changed the invalid-iota mishap to display the type of the offending iota along with the iota itself, by Robotgiggle in [#951](https://github.com/FallingColors/HexMod/pull/951).
- Changed the disallowed-action mishap to properly display the offending action, by Robotgiggle in [#970](https://github.com/FallingColors/HexMod/pull/970).

### Fixed

- Fixed missing dependency metadata on CurseForge/Modrinth.
- Fixed a freeze when rendering patterns in certain cases, by vgskye in [#800](https://github.com/FallingColors/HexMod/pull/800).
- Added a missing translation for `hexcasting.subtitles.casting.cast.fail`, by Robotgiggle in [#814](https://github.com/FallingColors/HexMod/pull/814).
- Fixed incorrect Amethyst Sconce block rotation, by Robotgiggle in [#814](https://github.com/FallingColors/HexMod/pull/814).
- Fixed Place Block not pulling items from the inventory when available, by garyantonyo in [#812](https://github.com/FallingColors/HexMod/pull/812).
- Fixed a broken link in A Primer On Vectors, by bearofbusiness in [#877](https://github.com/FallingColors/HexMod/pull/877).
- Fixed Impulse not increasing its cost for subsequent uses in one cast, by vgskye in [#853](https://github.com/FallingColors/HexMod/pull/853).
- Added a config option to enable commas between patterns in lists, by Robotgiggle in [#844](https://github.com/FallingColors/HexMod/pull/844).
- Fixed incorrect cost for Wayfarer's Flight, by Robotgiggle in [#844](https://github.com/FallingColors/HexMod/pull/844).
- Fixed a bug where some patterns would check for media requirements earlier than expected, by vgskye in [#855](https://github.com/FallingColors/HexMod/pull/855).
- Fixed some modded fake players being able to overcast infinitely for free, by vgskye in [#854](https://github.com/FallingColors/HexMod/pull/854).
- Fixed Negation Purification not working on numbers, by Robotgiggle in [#869](https://github.com/FallingColors/HexMod/pull/869).
- Added a missing translation for the `no_spell_circle` mishap, by Robotgiggle in [#862](https://github.com/FallingColors/HexMod/pull/862).
- Fixed allays sometimes becoming invulnerable when mindflayed, by navarchus in [#928](https://github.com/FallingColors/HexMod/pull/928).
- hexdoc: Improved the error message for missing patterns, by navarchus in [#931](https://github.com/FallingColors/HexMod/pull/931).
- Fixed incorrect rendering for rod variants of cyphers/trinkets/artifacts, by navarchus in [#936](https://github.com/FallingColors/HexMod/pull/936).
- Fixed several issues related to invalid/overlapping patterns, by object-Object in [#938](https://github.com/FallingColors/HexMod/pull/938).
- Fixed sticky teleportation (ie. teleporting passengers) not working (again), by Robotgiggle in [#916](https://github.com/FallingColors/HexMod/pull/916).
- Fixed Greater Teleport sometimes failing with a "moved wrongly" message, by Robotgiggle in [#916](https://github.com/FallingColors/HexMod/pull/916).
- Fixed incorrect face textures for the Empty and Shepherd Directrix, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Removed unnecessary error logs when activating a bound Cleric Impetus with its owner offline, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Fixed the Cleric Impetus becoming unbound when activated with its owner offline, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Fixed some directrices inadvertently accepting input from their output faces, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Added a missing translation for providing something other than an entity as the first input to Flay Mind, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Added several missing translations for config options, by Robotgiggle in [#921](https://github.com/FallingColors/HexMod/pull/921).
- Clarified the description of Division Distillation, by JustS-js in [#832](https://github.com/FallingColors/HexMod/pull/832).
- Fixed a bug where some patterns inconsistently checked the lower-north-west corner of blocks for ambit instead of the center, by YukkuriC in [#959](https://github.com/FallingColors/HexMod/pull/959).
- Fixed Erase Item cost not scaling with stack size, by PoolloverNathan in [#966](https://github.com/FallingColors/HexMod/pull/966).

## Previous versions

See https://www.curseforge.com/minecraft/mc-mods/hexcasting/files or https://modrinth.com/mod/hex-casting/changelog for changelogs from previous versions.
