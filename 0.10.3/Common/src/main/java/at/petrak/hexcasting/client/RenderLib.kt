@file:JvmName("RenderLib")

package at.petrak.hexcasting.client

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.TAU
import at.petrak.hexcasting.api.utils.getValue
import at.petrak.hexcasting.api.utils.setValue
import at.petrak.hexcasting.api.utils.weakMapped
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
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource
import net.minecraft.world.level.levelgen.synth.SimplexNoise
import net.minecraft.world.phys.Vec2
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

val NOISE: SimplexNoise = SimplexNoise(SingleThreadedRandomSource(9001L))

// see the test; perlin noise seems to output almost exclusively between -0.5 and 0.5
// i could probably impl this "properly" with some kind of exponent but it's faster and easier to divide
fun getNoise(x: Double, y: Double, z: Double): Double =
    NOISE.getValue(x * 0.6, y * 0.6, z * 0.6) / 2.0

// how many degrees are between each triangle on the smooth caps of the lines
const val CAP_THETA = 180f / 10f
const val DEFAULT_READABILITY_OFFSET = 0.2f
const val DEFAULT_LAST_SEGMENT_LEN_PROP = 0.8f

/**
 * Draw a sequence of linePoints spanning the given points.
 *
 * Please make sure to enable the right asinine shaders; see [GuiSpellcasting]
 */
fun drawLineSeq(
    mat: Matrix4f,
    points: List<Vec2>,
    width: Float,
    z: Float,
    tail: Int,
    head: Int,
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

    val n = points.size
    val joinAngles = FloatArray(n)
    val joinOffsets = FloatArray(n)
    for (i in 2 until n) {
        val p0 = points[i - 2];
        val p1 = points[i - 1];
        val p2 = points[i];
        val prev = p1.add(p0.negated())
        val next = p2.add(p1.negated())
        val angle =
            Mth.atan2((prev.x * next.y - prev.y * next.x).toDouble(), (prev.x * next.x + prev.y * next.y).toDouble())
                .toFloat()
        joinAngles[i - 1] = angle
        val clamp = Math.min(prev.length(), next.length()) / (width * 0.5f);
        joinOffsets[i - 1] = Mth.clamp(Mth.sin(angle) / (1 + Mth.cos(angle)), -clamp, clamp)
    }

    fun vertex(color: BlockPos, pos: Vec2) =
        buf.vertex(mat, pos.x, pos.y, z).color(color.x, color.y, color.z, a).endVertex()

    buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)
    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        // https://github.com/not-fl3/macroquad/blob/master/src/shapes.rs#L163
        // GuiComponent::innerFill line 52
        // fedor have useful variable names challenge (99% can't beat)
        val tangent = p2.add(p1.negated()).normalized().scale(width * 0.5f)
        val normal = Vec2(-tangent.y, tangent.x)

        fun color(time: Float): BlockPos =
            BlockPos(Mth.lerp(time, r1, r2).toInt(), Mth.lerp(time, g1, g2).toInt(), Mth.lerp(time, b1, b2).toInt())

        val color1 = color(i.toFloat() / n)
        val color2 = color((i + 1f) / n)
        val jlow = joinOffsets[i]
        val jhigh = joinOffsets[i + 1]
        // Draw the line segment as a hexagon, sort of
        // I can't imagine what the hell alwinfy is up to but this is implementing what TRIANGLE_FAN does
        // using normal triangles so we can send the entire segment to the buffer at once
        val p1Down = p1.add(tangent.scale(Math.max(0f, jlow))).add(normal)
        val p1Up = p1.add(tangent.scale(Math.max(0f, -jlow))).add(normal.negated())
        val p2Down = p2.add(tangent.scale(Math.max(0f, jhigh)).negated()).add(normal)
        val p2Up = p2.add(tangent.scale(Math.max(0f, -jhigh)).negated()).add(normal.negated())

        vertex(color1, p1Down)
        vertex(color1, p1)
        vertex(color1, p1Up)

        vertex(color1, p1Down)
        vertex(color1, p1Up)
        vertex(color2, p2Up)

        vertex(color1, p1Down)
        vertex(color2, p2Up)
        vertex(color2, p2)

        vertex(color1, p1Down)
        vertex(color2, p2)
        vertex(color2, p2Down)

        if (i > 0) {
            // Draw the connector to the next line segment
            val sangle = joinAngles[i]
            val angle = Math.abs(sangle)
            val rnormal = normal.negated()
            val joinSteps = Mth.ceil(angle * 180 / (CAP_THETA * Mth.PI))
            if (joinSteps < 1) {
                continue
            }

            if (sangle < 0) {
                var prevVert = Vec2(p1.x - rnormal.x, p1.y - rnormal.y)
                for (j in 1..joinSteps) {
                    val fan = rotate(rnormal, -sangle * (j.toFloat() / joinSteps))
                    val fanShift = Vec2(p1.x - fan.x, p1.y - fan.y)

                    vertex(color1, p1)
                    vertex(color1, prevVert)
                    vertex(color1, fanShift)
                    prevVert = fanShift
                }
            } else {
                val startFan = rotate(normal, -sangle)
                var prevVert = Vec2(p1.x - startFan.x, p1.y - startFan.y)
                for (j in joinSteps - 1 downTo 0) {
                    val fan = rotate(normal, -sangle * (j.toFloat() / joinSteps))
                    val fanShift = Vec2(p1.x - fan.x, p1.y - fan.y)

                    vertex(color1, p1)
                    vertex(color1, prevVert)
                    vertex(color1, fanShift)
                    prevVert = fanShift
                }
            }
        }
    }
    tess.end()

    fun drawCaps(color: BlockPos, point: Vec2, prev: Vec2) {
        val tangent = point.add(prev.negated()).normalized().scale(0.5f * width)
        val normal = Vec2(-tangent.y, tangent.x)
        val joinSteps = Mth.ceil(180f / CAP_THETA)
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR)
        vertex(color, point)
        for (j in joinSteps downTo 0) {
            val fan = rotate(normal, -Mth.PI * (j.toFloat() / joinSteps))
            buf.vertex(mat, point.x + fan.x, point.y + fan.y, z).color(color.x, color.y, color.z, a).endVertex()
        }
        tess.end()
    }
    drawCaps(BlockPos(r1.toInt(), g1.toInt(), b1.toInt()), points[0], points[1])
    drawCaps(BlockPos(r2.toInt(), g2.toInt(), b2.toInt()), points[n - 1], points[n - 2])
}

fun rotate(vec: Vec2, theta: Float): Vec2 {
    val cos = Mth.cos(theta)
    val sin = Mth.sin(theta)
    return Vec2(vec.x * cos - vec.y * sin, vec.y * cos + vec.x * sin)
}

/**
 * Draw a hex pattern from the given list of non-zappy points (as in, do the *style* of drawing it,
 * you have to do the conversion yourself.)
 */
fun drawPatternFromPoints(
    mat: Matrix4f,
    points: List<Vec2>,
    dupIndices: Set<Int>?,
    drawLast: Boolean,
    tail: Int,
    head: Int,
    flowIrregular: Float,
    readabilityOffset: Float,
    lastSegmentLenProportion: Float,
    seed: Double
) {
    val zappyPts = makeZappy(points, dupIndices, 10, 2.5f, 0.1f, flowIrregular, readabilityOffset, lastSegmentLenProportion, seed)
    val nodes = if (drawLast) {
        points
    } else {
        points.dropLast(1)
    }
    drawLineSeq(mat, zappyPts, 5f, 0f, tail, head)
    drawLineSeq(mat, zappyPts, 2f, 1f, screenCol(tail), screenCol(head))
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

/**
 * Split up a sequence of linePoints with a lightning effect
 * @param hops: rough number of points to subdivide each segment into
 * @param speed: rate at which the lightning effect should move/shake/etc
 */
fun makeZappy(
    barePoints: List<Vec2>, dupIndices: Set<Int>?, hops: Int, variance: Float, speed: Float, flowIrregular: Float,
    readabilityOffset: Float, lastSegmentLenProportion: Float, seed: Double
): List<Vec2> {
    // Nothing in, nothing out
    if (barePoints.isEmpty()) {
        return emptyList()
    }
    fun zappify(points: List<Vec2>, truncateLast: Boolean): List<Vec2> {
        val scaleVariance = { it: Double -> 1.0.coerceAtMost(8 * (0.5 - abs(0.5 - it))) }
        val zSeed = ClientTickCounter.getTotal().toDouble() * speed
        // Create our output list of zap points
        val zappyPts = ArrayList<Vec2>(points.size * hops)
        zappyPts.add(points[0])
        // For each segment in the original...
        for ((i, pair) in points.zipWithNext().withIndex()) {
            val (src, target) = pair
            val delta = target.add(src.negated())
            // Take hop distance
            val hopDist = Mth.sqrt(src.distanceToSqr(target)) / hops
            // Compute how big the radius of variance should be
            val maxVariance = hopDist * variance

            // for a list of length n, there will be n-1 pairs,
            // and so the last index will be (n-1)-1
            val maxJ = if (truncateLast && i == points.size - 2) {
                (lastSegmentLenProportion * hops.toFloat()).roundToInt()
            } else hops

            for (j in 1..maxJ) {
                val progress = j.toDouble() / (hops + 1)
                // Add the next hop...
                val pos = src.add(delta.scale(progress.toFloat()))
                // as well as some random variance...
                // (We use i, j (segment #, subsegment #) as seeds for the Perlin noise,
                // and zSeed (i.e. time elapsed) to perturb the shape gradually over time)
                val minorPerturb = getNoise(i.toDouble(), j.toDouble(), sin(zSeed)) * flowIrregular
                val theta = (3 * getNoise(
                    i + progress + minorPerturb - zSeed,
                    1337.0,
                    seed
                ) * TAU).toFloat()
                val r = (getNoise(
                    i + progress - zSeed,
                    69420.0,
                    seed
                ) * maxVariance * scaleVariance(progress)).toFloat()
                val randomHop = Vec2(r * Mth.cos(theta), r * Mth.sin(theta))
                // Then record the new location.
                zappyPts.add(pos.add(randomHop))

                if (j == hops) {
                    // Finally, we hit the destination, add that too
                    // but we might not hit the destination if we want to stop short
                    zappyPts.add(target)
                }
            }
        }
        return zappyPts
    }

    val points = mutableListOf<Vec2>()
    val daisyChain = mutableListOf<Vec2>()
    return if (dupIndices != null) {
        for ((i, pair) in barePoints.zipWithNext().withIndex()) {
            val (head, tail) = pair
            val tangent = tail.add(head.negated()).scale(readabilityOffset)
            if (i != 0 && dupIndices.contains(i)) {
                daisyChain.add(head.add(tangent))
            } else {
                daisyChain.add(head)
            }
            if (i == barePoints.size - 2) {
                daisyChain.add(tail)
                points.addAll(zappify(daisyChain, true))
            } else if (dupIndices.contains(i + 1)) {
                daisyChain.add(tail.add(tangent.negated()))
                points.addAll(zappify(daisyChain, false))
                daisyChain.clear()
            }
        }
        points
    } else {
        zappify(barePoints, true)
    }
}

fun <T> findDupIndices(pts: Iterable<T>): Set<Int> {
    val dedup = HashMap<T, Int>()
    val found = HashSet<Int>()
    for ((i, pt) in pts.withIndex()) {
        val ix = dedup[pt]
        if (ix != null) {
            found.add(i)
            found.add(ix)
        } else {
            dedup.put(pt, i)
        }
    }
    return found
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
