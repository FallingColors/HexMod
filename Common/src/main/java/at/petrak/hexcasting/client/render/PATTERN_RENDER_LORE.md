# Pattern Rendering Lore

This is an overview of the new pattern rendering systems introduced alongside the Inline pattern rendering 

## Brief History / Motivations

In v0.10.3 (and probably before) the pattern rendering was well known for causing lag if many patterns were rendered at once. 
The pattern code was also duplicated *a lot*, pretty much anywhere that did pattern rendering needed to do it slightly different and so the rendering/positioning code got copy-pasted all around, frequently with a lot of magic numbers. 

During 1.20 development, we [added texture based rendering](https://github.com/FallingColors/HexMod/pull/555), and switched most static rendering over to it. There was still a fair bit of duplicate code though, especially with pattern positioning. 

Now with the new system, all of the rendering is contained to a few classes and outside users (such as slates for example) can specify how they want patterns to be rendered using PatternSettings and PatternColors.

## System Walkthrough (External)

### PatternRenderer

This is the main entrypoint for pattern rendering. It has 3 main methods, all called `renderPattern`. One is the driver method and the others are convenience wrappers.

Generally the idea here is that you shouldn't need to worry about whether the pattern will be rendered as a texture or dynamically, the `PatternRenderer` will make that decision, prefering the texture renderer when it can. The dynamic renderer will be used if the pattern is moving (speed != 0), if the pattern has a gradient stroke, or if the texture isn't ready yet.

### PatternSettings

This is where the vast majority of the rendering configuration happens. Arguably it is overkill. 

It's a class with many getters constructed from 3 records: `PositionSettings`, `StrokeSettings`, and `ZappySettings`. The getters can be overridden when/if needed, the records are more for user convenience. See javadocs for details on what can be configured here.

Pattern textures are also generated based on settings, so it's **VERY ENCOURAGED** to re-use pattern settings when you can.

### PatternColors

This is just a simple record holding colors for different parts of pattern drawing. It has probably too many helpers. The main thing to note here is that you can set the alpha to 0 to skip rendering a section (such as dots or innerStroke). Transparent colors for strokes are **discouraged** due to the dynamic renderer having a sort of internal overlapping that is only noticeable with transparent strokes.

### WorldlyPatternRenderHelpers

This is where all the worldly base-hex renders ended up. Good to look at for examples of using the renderer and some pattern settings that could be re-used.

## System Walkthrough (Internal)

### HexPatternPoints

This is where the positioning actually happens. It generates dots and zappy points based on the pattern and PatternSettings passed in. This object is then cached to prevent needing to calculate it all each frame. Note that this includes scaling and all that, the returned zappy points are in pose units.

### VCDrawHelper (& RenderLib changes)

We do a silly with this one lol. This allows us to separate the lower level vertex handling from the higher level 'drawing'. 

Previously `RenderLib.drawLineSeq(..)` drew straight to the tesselator with the `POSITION_COLOR` shader/format. Now it just passes color and position data to the `VCDrawHelper` that we give it, allowing us to create and push a vertex however we want to wherever we want. This lets us draw to other vertex consumers and use other shaders/formats, like the `EntityTranslucentCull` that we use for worldly rendering with light and normals.

To maintain API stability we have the previous `RenderLib.drawLineSeq(..)` method signature just call the new version using the `Basic` draw helper.

Conveniently we can also use this for drawing our textures!