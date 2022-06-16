@file:JvmName("RenderLib")

package at.petrak.hexcasting.client

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.TAU
import at.petrak.hexcasting.api.utils.getValue
import at.petrak.hexcasting.api.utils.setValue
import at.petrak.hexcasting.api.utils.weakMapped
import at.petrak.hexcasting.client.gui.GuiSpellcasting
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import net.minecraft.world.level.levelgen.synth.PerlinNoise
import net.minecraft.world.phys.Vec2
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

/**
 * Source of perlin noise
 */
val NOISE: PerlinNoise = PerlinNoise.create(XoroshiroRandomSource(9001L), listOf(0, 1, 2, 3, 4))

/**
 * Draw a sequence of linePoints spanning the given points.
 *
 * Please make sure to enable the right asinine shaders; see [GuiSpellcasting]
 */
@JvmOverloads
fun drawLineSeq(
    mat: Matrix4f,
    points: List<Vec2>,
    width: Float,
    z: Float,
    tail: Int,
    head: Int,
    animTime: Float? = null,
) {
    if (points.size <= 1) return

    val r1 = FastColor.ARGB32.red(tail).toFloat()
    val g1 = FastColor.ARGB32.green(tail).toFloat()
    val b1 = FastColor.ARGB32.blue(tail).toFloat()
    val a = FastColor.ARGB32.alpha(tail)
    val headSource = if (Screen.hasControlDown() != HexConfig.client().ctrlTogglesOffStrokeOrder())
        head
    else
        tail
    val r2 = FastColor.ARGB32.red(headSource).toFloat()
    val g2 = FastColor.ARGB32.green(headSource).toFloat()
    val b2 = FastColor.ARGB32.blue(headSource).toFloat()

    // they spell it wrong at mojang lmao
    val tess = Tesselator.getInstance()
    val buf = tess.builder

    // We use one single TRIANGLE_STRIP
    // in order to connect adjacent segments together and not get the weird hinge effect.
    // There's still some artifacting but this is passable, at least.
    buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR)

    val n = points.size
    for ((i, pair) in points.zipWithNext().withIndex()) {
        val (p1, p2) = pair
        // https://github.com/not-fl3/macroquad/blob/master/src/shapes.rs#L163
        // GuiComponent::innerFill line 52
        // fedor have useful variable names challenge (99% can't beat)
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        // normal x and y, presumably?
        val nx = -dy
        val ny = dx
        // thickness?
        val tlen = Mth.sqrt(nx * nx + ny * ny) / (width * 0.5f)
        val tx = nx / tlen
        val ty = ny / tlen

        fun color(time: Float): BlockPos =
            BlockPos(Mth.lerp(time, r1, r2).toInt(), Mth.lerp(time, g1, g2).toInt(), Mth.lerp(time, b1, b2).toInt())

        val color1 = color(i.toFloat() / n)
        val color2 = color((i + 1f) / n)
        buf.vertex(mat, p1.x + tx, p1.y + ty, z).color(color1.x, color1.y, color1.z, a).endVertex()
        buf.vertex(mat, p1.x - tx, p1.y - ty, z).color(color1.x, color1.y, color1.z, a).endVertex()
        buf.vertex(mat, p2.x + tx, p2.y + ty, z).color(color2.x, color2.y, color2.z, a).endVertex()
        buf.vertex(mat, p2.x - tx, p2.y - ty, z).color(color2.x, color2.y, color2.z, a).endVertex()
    }
    tess.end()

    if (animTime != null) {
        val pointCircuit =
            (animTime * 30f * HexConfig.client().patternPointSpeedMultiplier().toFloat()) % (points.size + 10)
        // subtract 1 to avoid the point appearing between the end and start for 1 frame
        if (pointCircuit < points.size - 1) {
            val pointMacro = floor(pointCircuit).toInt()
            val pointMicro = pointCircuit - pointMacro

            val p1 = points[pointMacro]
            val p2 = points[(pointMacro + 1) % points.size]
            val drawPos = Vec2(
                p1.x + (p2.x - p1.x) * pointMicro,
                p1.y + (p2.y - p1.y) * pointMicro,
            )
            drawSpot(
                mat,
                drawPos,
                2f,
                (r1 + 255) / 2f / 255f,
                (g1 + 255) / 2f / 255f,
                (b1 + 255) / 2f / 255f,
                a / 1.2f / 255f
            )
        }
    }
}

/**
 *  * Draw a hex pattern from the given list of non-zappy points (as in, do the *style* of drawing it,
 *   * you have to do the conversion yourself.)
 *    */
@JvmOverloads
fun drawPatternFromPoints(
    mat: Matrix4f,
    points: List<Vec2>,
    drawLast: Boolean,
    tail: Int,
    head: Int,
    flowIrregular: Float,
    animTime: Float? = null
) {
    val zappyPts = makeZappy(points, 10f, 2.5f, 0.1f, flowIrregular)
    val nodes = if (drawLast) {
        points
    } else {
        points.dropLast(1)
    }
    drawLineSeq(mat, zappyPts, 5f, 0f, tail, head, null)
    drawLineSeq(mat, zappyPts, 2f, 1f, screenCol(tail), screenCol(head), animTime)
    for (node in nodes) {
        drawSpot(
            mat,
            node,
            2f,
            dodge(FastColor.ARGB32.red(head)) / 255f,
            dodge(FastColor.ARGB32.green(head)) / 255f,
            dodge(FastColor.ARGB32.blue(head)) / 255f,
            FastColor.ARGB32.alpha(head) / 255f
        );
    }
}

fun makeZappy(points: List<Vec2>, hops: Float, variance: Float, speed: Float, flowIrregular: Float) =
    makeZappy(points, hops.toInt(), variance, speed, flowIrregular)

/**
 * Split up a sequence of linePoints with a lightning effect
 * @param hops: rough number of points to subdivide each segment into
 * @param speed: rate at which the lightning effect should move/shake/etc
 */
fun makeZappy(points: List<Vec2>, hops: Int, variance: Float, speed: Float, flowIrregular: Float): List<Vec2> {
    // Nothing in, nothing out
    if (points.isEmpty()) {
        return emptyList()
    }
    val scaleVariance = { it: Double -> 1.0.coerceAtMost(8 * (0.5 - abs(0.5 - it))) }
    val zSeed = ClientTickCounter.getTotal().toDouble() * speed
    // Create our output list of zap points
    val zappyPts = mutableListOf(points[0])
    // For each segment in the original...
    for ((i, pair) in points.zipWithNext().withIndex()) {
        val (src, target) = pair
        val delta = target.add(src.negated())
        // Take hop distance
        val hopDist = Mth.sqrt(src.distanceToSqr(target)) / hops
        // Compute how big the radius of variance should be
        val maxVariance = hopDist * variance

        for (j in 1..hops) {
            val progress = j.toDouble() / (hops + 1)
            // Add the next hop...
            val pos = src.add(delta.scale(progress.toFloat()))
            // as well as some random variance...
            // (We use i, j (segment #, subsegment #) as seeds for the Perlin noise,
            // and zSeed (i.e. time elapsed) to perturb the shape gradually over time)
            val minorPerturb = NOISE.getValue(i.toDouble(), j.toDouble(), sin(zSeed)) * flowIrregular
            val theta = (3 * NOISE.getValue(
                i.toDouble() + j.toDouble() / (hops + 1) + minorPerturb - zSeed,
                1337.0,
                0.0
            ) * TAU).toFloat()
            val r = (NOISE.getValue(
                i.toDouble() + j.toDouble() / (hops + 1) - zSeed,
                69420.0,
                0.0
            ) * maxVariance * scaleVariance(progress)).toFloat()
            val randomHop = Vec2(r * Mth.cos(theta), r * Mth.sin(theta))
            // Then record the new location.
            zappyPts.add(pos.add(randomHop))
        }
        // Finally, we hit the destination, add that too
        zappyPts.add(target)
    }
    return zappyPts
}

/**
 * Draw a little circle, because Minecraft rendering code is a nightmare and doesn't
 * include primitive drawing code...
 */
fun drawSpot(mat: Matrix4f, point: Vec2, radius: Float, r: Float, g: Float, b: Float, a: Float) {
    val tess = Tesselator.getInstance()
    val buf = tess.builder
    // https://stackoverflow.com/questions/20394727/gl-triangle-strip-vs-gl-triangle-fan
    // Starting point is the center
    buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR)
    buf.vertex(mat, point.x, point.y, 1f).color(r, g, b, a).endVertex()

    // https://github.com/not-fl3/macroquad/blob/master/src/shapes.rs#L98
    // yes they are gonna be little hexagons fite me
    val fracOfCircle = 6
    // run 0 AND last; this way the circle closes
    for (i in 0..fracOfCircle) {
        val theta = i.toFloat() / fracOfCircle * TAU.toFloat()
        val rx = Mth.cos(theta) * radius + point.x
        val ry = Mth.sin(theta) * radius + point.y
        buf.vertex(mat, rx, ry, 1f).color(r, g, b, a).endVertex()
    }

    tess.end()
}

fun screenCol(n: Int): Int {
    return FastColor.ARGB32.color(
        FastColor.ARGB32.alpha(n),
        screen(FastColor.ARGB32.red(n)),
        screen(FastColor.ARGB32.green(n)),
        screen(FastColor.ARGB32.blue(n)),
    )
}

fun screen(n: Int) = (n + 255) / 2
fun dodge(n: Int) = n * 0.9f

/**
 * Return the scale and dots formed by the pattern when centered.
 */
fun getCenteredPattern(pattern: HexPattern, width: Float, height: Float, minSize: Float): Pair<Float, List<Vec2>> {
    // Do two passes: one with a random size to find a good COM and one with the real calculation
    val com1: Vec2 = pattern.getCenter(1f)
    val lines1: List<Vec2> = pattern.toLines(1f, Vec2.ZERO)
    var maxDx = -1f
    var maxDy = -1f
    for (dot in lines1) {
        val dx = Mth.abs(dot.x - com1.x)
        if (dx > maxDx) {
            maxDx = dx
        }
        val dy = Mth.abs(dot.y - com1.y)
        if (dy > maxDy) {
            maxDy = dy
        }
    }
    val scale =
        min(minSize, min(width / 3f / maxDx, height / 3f / maxDy))
    val com2: Vec2 = pattern.getCenter(scale)
    val lines2: List<Vec2> = pattern.toLines(scale, com2.negated())
    return scale to lines2
}

fun renderItemStackInGui(ms: PoseStack, stack: ItemStack, x: Int, y: Int) {
    transferMsToGl(ms) { Minecraft.getInstance().itemRenderer.renderAndDecorateItem(stack, x, y) }
}

fun transferMsToGl(ms: PoseStack, toRun: Runnable) {
    val mvs = RenderSystem.getModelViewStack()
    mvs.pushPose()
    mvs.mulPoseMatrix(ms.last().pose())
    RenderSystem.applyModelViewMatrix()
    toRun.run()
    mvs.popPose()
    RenderSystem.applyModelViewMatrix()
}

private var villager: Villager? by weakMapped(Villager::level)

fun prepareVillagerForRendering(ingredient: VillagerIngredient, level: Level): Villager {
    val minLevel: Int = ingredient.minLevel()
    val profession: VillagerProfession = Registry.VILLAGER_PROFESSION.getOptional(ingredient.profession())
        .orElse(VillagerProfession.NONE)
    val biome: VillagerType = Registry.VILLAGER_TYPE.getOptional(ingredient.biome())
        .orElse(VillagerType.PLAINS)

    val instantiatedVillager = villager ?: run {
        val newVillager = Villager(EntityType.VILLAGER, level)
        villager = newVillager
        newVillager
    }

    instantiatedVillager.villagerData = instantiatedVillager.villagerData
        .setProfession(profession)
        .setType(biome)
        .setLevel(minLevel)

    return instantiatedVillager
}

@JvmOverloads
fun renderEntity(
    ms: PoseStack, entity: Entity, world: Level, x: Float, y: Float, rotation: Float,
    renderScale: Float, offset: Float,
    bufferTransformer: (MultiBufferSource) -> MultiBufferSource = { it -> it }
) {
    entity.level = world
    ms.pushPose()
    ms.translate(x.toDouble(), y.toDouble(), 50.0)
    ms.scale(renderScale, renderScale, renderScale)
    ms.translate(0.0, offset.toDouble(), 0.0)
    ms.mulPose(Vector3f.ZP.rotationDegrees(180.0f))
    ms.mulPose(Vector3f.YP.rotationDegrees(rotation))
    val erd = Minecraft.getInstance().entityRenderDispatcher
    val immediate = Minecraft.getInstance().renderBuffers().bufferSource()
    erd.setRenderShadow(false)
    erd.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, ms, bufferTransformer(immediate), 0xf000f0)
    erd.setRenderShadow(true)
    immediate.endBatch()
    ms.popPose()
}

/**
 * Make sure you have the `PositionColorShader` set
 */
fun renderQuad(
    ps: PoseStack, x: Float, y: Float, w: Float, h: Float, color: Int
) {
    val mat = ps.last().pose()
    val tess = Tesselator.getInstance()
    val buf = tess.builder
    buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
    buf.vertex(mat, x, y, 0f)
        .color(color)
        .endVertex()
    buf.vertex(mat, x, y + h, 0f)
        .color(color)
        .endVertex()
    buf.vertex(mat, x + w, y + h, 0f)
        .color(color)
        .endVertex()
    buf.vertex(mat, x + w, y, 0f)
        .color(color)
        .endVertex()
    tess.end()
}
