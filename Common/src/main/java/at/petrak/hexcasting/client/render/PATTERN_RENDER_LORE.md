# Pattern Rendering Lore

This is an overview of the new pattern rendering systems introduced alongside the Inline pattern rendering 

## Brief History / Motivations

In v0.10.3 (and probably before) the pattern rendering was well known for causing lag if many patterns were rendered at once. 
The pattern code was also duplicated *a lot*, pretty much anywhere that did pattern rendering needed to do it slightly different and so the rendering/positioning code got copy-pasted all around, frequently with a lot of magic numbers. 

During 1.20 development, we [added texture based rendering](https://github.com/FallingColors/HexMod/pull/555), and switched most static rendering over to it. There was still a fair bit of duplicate code though, especially with pattern positioning. 

Now with the new system, all of the rendering is contained to a few classes and outside users (such as slates for example) can specify how they want patterns to be rendered using PatternSettings and PatternColors.

## System Walkthrough

### PatternRenderer

This is the main entrypoint for pattern rendering. It has 2 main methods, both called `renderPattern`. The only difference is that one has a `WorldlyBits` argument used for passing in lighting, vc providers, and normals, that are generally only used for in-world rendering, as opposed to UI rendering.

Generally the idea here is that you shouldn't need to worry about whether the pattern will be rendered as a texture or dynamically, the `PatternRenderer` will make that decision, prefering the texture renderer when it can. 