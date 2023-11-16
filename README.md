# Hex Casting

[Curseforge](https://www.curseforge.com/minecraft/mc-mods/hexcasting) | [Modrinth](https://modrinth.com/mod/hex-casting)
| [Source](https://github.com/gamma-delta/HexMod)

A minecraft mod about casting Hexes, powerful and programmable magical effects, inspired by PSI.

[Curseforge Link](https://www.curseforge.com/minecraft/mc-mods/hexcasting)

On Forge, this mod requires:

- PAUCAL
- Patchouli
- Kotlin for Forge
- Caelus elytra api

On Fabric, it requires:

- PAUCAL
- Patchouli
- Fabric Language Kotlin
- Cardinal Components
- ClothConfig and ModMenu

- [Read the documentation online here!](https://gamma-delta.github.io/HexMod/)
- [Discord link](https://discord.gg/4xxHGYteWk)

## The Branches

We are currently developing Hexcasting v1.x for 1.19.2, on the `main` branch.

The 0.9.x versions, for 1.18.2, are in long-term support. We probably won't be adding any new features, but we will try
to fix bugs. Those are on the `1.18` branch.

The `gh-pages` branch is for the online Hex book.

Other branches are old detritus from potential features.

## For Developers

We publish artifacts on Maven at [https://maven.blamejared.com/at/petra-k/hexcasting/]. The modern coordinates are at:

> `hexcasting-[PLATFORM]-[MC VERSION]/[MOD VERSION]`

There are some other folders in the `hexcasting` folder from old CI configurations; ignore those, they're stale.

Please only use things in the `at.petrak.hexcasting.api` package. (We do try to keep the API fairly stable, but we don't
do a very good job.) If you find you need something not in there yell at me on Discord.
