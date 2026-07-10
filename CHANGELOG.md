# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [UNRELEASED]

### Added

- Updated to Minecraft 1.21.1 ([#985](https://github.com/FallingColors/HexMod/pull/985)) @SuperKnux @slava110
- Added the `hex_unbreakable` tag for blocks that should be immune to Break Block regardless of the configured mining tier ([#1186](https://github.com/FallingColors/HexMod/pull/1186)) @Robotgiggle @slava110
- Added Coalescing Distillation, which performs the null-coalescing operation ([#1196](https://github.com/FallingColors/HexMod/pull/1196)) @Robotgiggle

### Fixed

- Fixed Entity Iota comparison to use `equals` on entity IDs instead of reference equality ([#1101](https://github.com/FallingColors/HexMod/pull/1101)) @IridescentVoid
- Corrected field names of the codec for Pattern Iotas and add a graceful fallback for upgrading ([#1120](https://github.com/FallingColors/HexMod/pull/1120), [#1131](https://github.com/FallingColors/HexMod/pull/1131), [#1140](https://github.com/FallingColors/HexMod/pull/1140)) @Master-Bw3 @IridescentVoid

### Internal

- Removed APIs deprecated in `0.11.4` ([#1126](https://github.com/FallingColors/HexMod/pull/1126), [#1127](https://github.com/FallingColors/HexMod/pull/1127) [#1129](https://github.com/FallingColors/HexMod/pull/1129), [#1137](https://github.com/FallingColors/HexMod/pull/1137), [#1142](https://github.com/FallingColors/HexMod/pull/1142)) @s5bug

## `0.11.3` - 2025-11-22

### Added

- Added several new player attributes (ported from [Hexxy Attributes](https://modrinth.com/mod/hexxyattributes)) ([#823](https://github.com/FallingColors/HexMod/pull/823)) @beholderface
- Added ancient cyphers ([#838](https://github.com/FallingColors/HexMod/pull/838)) @Robotgiggle
- Added the ability for wandering traders to sell ancient scrolls ([#837](https://github.com/FallingColors/HexMod/pull/837)) @Robotgiggle
- Added a creative tab for ancient scrolls ([#837](https://github.com/FallingColors/HexMod/pull/837)) @Robotgiggle
- Added a config option to disable Greater Teleport item splatting ([#841](https://github.com/FallingColors/HexMod/pull/841)) @TheDrawingCoding-Gamer
- Added loot tables to generate exactly one random scroll/cypher ([#844](https://github.com/FallingColors/HexMod/pull/844)) @Robotgiggle
- Added recipes for slate, amethyst, and quenched allay blocks ([#903](https://github.com/FallingColors/HexMod/pull/903)) @KyanBirb
- hexdoc: Added the ability to load patterns from a JSON file (instead of scraping them from source code with regex) ([#911](https://github.com/FallingColors/HexMod/pull/911)) @object-Object
- Added a new Ancient Pigment and changed the appearance of the default pigment ([#912](https://github.com/FallingColors/HexMod/pull/912)) @Robotgiggle

### Changed

- Updated the spell circle documentation to clarify their new behavior in 0.11.x ([#814](https://github.com/FallingColors/HexMod/pull/814)) @Robotgiggle
- Added the recipes for bamboo and cherry staves to the notebook entry ([#814](https://github.com/FallingColors/HexMod/pull/814)) @Robotgiggle
- Renamed the eval limit mishap from "Delve Too Deep" to "Lost in Thought", and updated the description and error message to match ([#814](https://github.com/FallingColors/HexMod/pull/814)) @Robotgiggle
- Updated zh_cn translations ([#799](https://github.com/FallingColors/HexMod/pull/799), [#828](https://github.com/FallingColors/HexMod/pull/828), [#847](https://github.com/FallingColors/HexMod/pull/847), [#913](https://github.com/FallingColors/HexMod/pull/913), [#953](https://github.com/FallingColors/HexMod/pull/953), [#954](https://github.com/FallingColors/HexMod/pull/954), [#971](https://github.com/FallingColors/HexMod/pull/971)) @ChuijkYahus
- Added apostrophes to Compass' Purification in the notebook ([#867](https://github.com/FallingColors/HexMod/pull/867)) @kineticneticat
- Updated the description of Ignite to clarify that it works on all entities ([#844](https://github.com/FallingColors/HexMod/pull/844)) @Robotgiggle
- Improved documentation of iota embedding ([#862](https://github.com/FallingColors/HexMod/pull/862)) @Robotgiggle
- Documented the behaviour of the Shepherd Directrix when no boolean is present on the stack ([#862](https://github.com/FallingColors/HexMod/pull/862)) @Robotgiggle
- Changed vectors in the notebook to use parentheses rather than square brackets ([#862](https://github.com/FallingColors/HexMod/pull/862)) @Robotgiggle
- Changed the rarity of several post-enlightenment items ([#860](https://github.com/FallingColors/HexMod/pull/860)) @Robotgiggle
- API: Changed the base class of `Mishap` from `Throwable` to `RuntimeException` to reduce the likelihood of server crashes ([#933](https://github.com/FallingColors/HexMod/pull/933)) @navarchus
- API: Added `@Throws` annotation to several `Action` methods to allow Java pattern implementations to throw mishaps ([#935](https://github.com/FallingColors/HexMod/pull/935)) @navarchus
- Improved the documentation for the media cube ([#843](https://github.com/FallingColors/HexMod/pull/843)) @object-Object
- Changed Greater Teleport to mishap when a passenger is immune to teleportation ([#916](https://github.com/FallingColors/HexMod/pull/916)) @Robotgiggle
- Re-added the slate limit for spell circles ([#909](https://github.com/FallingColors/HexMod/pull/909)) @Stickia
- Renamed Inverse Tangent Purification II to Inverse Tangent Distillation ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Massively improved ru_ru translations ([#832](https://github.com/FallingColors/HexMod/pull/832)) @JustS-js and LedinecMing
- Changed the invalid-pattern mishap to display the offending pattern ([#951](https://github.com/FallingColors/HexMod/pull/951)) @Robotgiggle
- Changed the invalid-iota mishap to display the type of the offending iota along with the iota itself ([#951](https://github.com/FallingColors/HexMod/pull/951)) @Robotgiggle
- Changed the disallowed-action mishap to properly display the offending action ([#970](https://github.com/FallingColors/HexMod/pull/970)) @Robotgiggle

### Fixed

- Fixed missing dependency metadata on CurseForge/Modrinth.
- Fixed a freeze when rendering patterns in certain cases ([#800](https://github.com/FallingColors/HexMod/pull/800)) @vgskye
- Added a missing translation for `hexcasting.subtitles.casting.cast.fail` ([#814](https://github.com/FallingColors/HexMod/pull/814)) @Robotgiggle
- Fixed incorrect Amethyst Sconce block rotation ([#814](https://github.com/FallingColors/HexMod/pull/814)) @Robotgiggle
- Fixed Place Block not pulling items from the inventory when available ([#812](https://github.com/FallingColors/HexMod/pull/812)) @garyantonyo
- Fixed a broken link in A Primer On Vectors ([#877](https://github.com/FallingColors/HexMod/pull/877)) @bearofbusiness
- Fixed Impulse not increasing its cost for subsequent uses in one cast ([#853](https://github.com/FallingColors/HexMod/pull/853)) @vgskye
- Added a config option to enable commas between patterns in lists ([#844](https://github.com/FallingColors/HexMod/pull/844)) @Robotgiggle
- Fixed incorrect cost for Wayfarer's Flight ([#844](https://github.com/FallingColors/HexMod/pull/844)) @Robotgiggle
- Fixed a bug where some patterns would check for media requirements earlier than expected ([#855](https://github.com/FallingColors/HexMod/pull/855)) @vgskye
- Fixed some modded fake players being able to overcast infinitely for free ([#854](https://github.com/FallingColors/HexMod/pull/854)) @vgskye
- Fixed Negation Purification not working on numbers ([#869](https://github.com/FallingColors/HexMod/pull/869)) @Robotgiggle
- Added a missing translation for the `no_spell_circle` mishap ([#862](https://github.com/FallingColors/HexMod/pull/862)) @Robotgiggle
- Fixed allays sometimes becoming invulnerable when mindflayed ([#928](https://github.com/FallingColors/HexMod/pull/928)) @navarchus
- hexdoc: Improved the error message for missing patterns ([#931](https://github.com/FallingColors/HexMod/pull/931)) @navarchus
- Fixed incorrect rendering for rod variants of cyphers/trinkets/artifacts ([#936](https://github.com/FallingColors/HexMod/pull/936)) @navarchus
- Fixed several issues related to invalid/overlapping patterns ([#938](https://github.com/FallingColors/HexMod/pull/938)) @object-Object
- Fixed sticky teleportation (ie. teleporting passengers) not working (again) ([#916](https://github.com/FallingColors/HexMod/pull/916)) @Robotgiggle
- Fixed Greater Teleport sometimes failing with a "moved wrongly" message ([#916](https://github.com/FallingColors/HexMod/pull/916)) @Robotgiggle
- Fixed incorrect face textures for the Empty and Shepherd Directrix ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Removed unnecessary error logs when activating a bound Cleric Impetus with its owner offline ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Fixed the Cleric Impetus becoming unbound when activated with its owner offline ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Fixed some directrices inadvertently accepting input from their output faces ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Added a missing translation for providing something other than an entity as the first input to Flay Mind ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Added several missing translations for config options ([#921](https://github.com/FallingColors/HexMod/pull/921)) @Robotgiggle
- Clarified the description of Division Distillation ([#832](https://github.com/FallingColors/HexMod/pull/832)) @JustS-js
- Fixed a bug where some patterns inconsistently checked the lower-north-west corner of blocks for ambit instead of the center ([#959](https://github.com/FallingColors/HexMod/pull/959)) @YukkuriC
- Fixed Erase Item cost not scaling with stack size ([#966](https://github.com/FallingColors/HexMod/pull/966)) @PoolloverNathan

## Previous versions

See https://www.curseforge.com/minecraft/mc-mods/hexcasting/files or https://modrinth.com/mod/hex-casting/changelog for changelogs from previous versions.
