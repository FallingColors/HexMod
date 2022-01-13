package at.petrak.hex.client

import at.petrak.hex.HexUtils
import at.petrak.hex.client.gui.SQRT_3
import at.petrak.hex.hexmath.HexCoord
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Matrix4f
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import net.minecraft.world.level.levelgen.synth.PerlinNoise
import net.minecraft.world.phys.Vec2
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Common draw code
 */
object RenderLib {
    /**
     * Source of perlin noise
     */
    val NOISE = PerlinNoise.create(XoroshiroRandomSource(9001L), listOf(0, 1, 2, 3, 4))


    /**
     * Draw a sequence of linePoints spanning the given points.
     *
     * Please make sure to enable the right asinine shaders; see [GuiSpellcasting][at.petrak.hex.client.gui.GuiSpellcasting]
     */
    @JvmStatic
    @JvmOverloads
    fun drawLineSeq(
        mat: Matrix4f,
        points: List<Vec2>,
        width: Float,
        z: Float,
        r: Int,
        g: Int,
        b: Int,
        a: Int,
        animTime: Float? = null,
        animDelta: Float = 0.5f,
        animMid: Float = 0.5f
    ) {
        if (points.size <= 1) return

        // they spell it wrong at mojang lmao
        val tess = Tesselator.getInstance()
        val buf = tess.builder

        // We use one single TRIANGLE_STRIP
        // in order to connect adjacent segments together and not get the weird hinge effect.
        // There's still some artifacting but this is passable, at least.
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR)

        var idx = 0
        for ((p1, p2) in points.zipWithNext()) {
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

            // https://github.com/gamma-delta/haxagon/blob/main/assets/shaders/pattern_beam.frag
            fun colAmt(t: Float): Float =
                if (animTime != null) {
                    val speed = 120f
                    // 0.8f factor here to make there a small pause between the start and end of the pattern
                    Mth.cos(Mth.PI * t * 0.8f - animTime * speed / points.size).absoluteValue.pow(100) * animDelta + animMid
                } else {
                    1f
                }

            val amt1 = colAmt(idx.toFloat() / points.size)
            val amt2 = colAmt((idx + 1).toFloat() / points.size)
            val r1 = Mth.clamp((r.toFloat() * amt1).toInt(), 0, 255)
            val g1 = Mth.clamp((g.toFloat() * amt1).toInt(), 0, 255)
            val b1 = Mth.clamp((b.toFloat() * amt1).toInt(), 0, 255)
            val r2 = Mth.clamp((r.toFloat() * amt2).toInt(), 0, 255)
            val g2 = Mth.clamp((g.toFloat() * amt2).toInt(), 0, 255)
            val b2 = Mth.clamp((b.toFloat() * amt2).toInt(), 0, 255)


            buf.vertex(mat, p1.x + tx, p1.y + ty, z).color(r1, g1, b1, a).endVertex()
            buf.vertex(mat, p1.x - tx, p1.y - ty, z).color(r1, g1, b1, a).endVertex()
            buf.vertex(mat, p2.x + tx, p2.y + ty, z).color(r2, g2, b2, a).endVertex()
            buf.vertex(mat, p2.x - tx, p2.y - ty, z).color(r2, g2, b2, a).endVertex()
            idx++
        }

        tess.end()
    }

    /**
     * Draw a hex pattern from the given list of non-zappy points (as in, do the *style* of drawing it,
     * you have to do the conversion yourself.)
     */
    @JvmStatic
    fun drawPattern(mat: Matrix4f, points: List<Vec2>, r: Int, g: Int, b: Int, a: Int, animTime: Float? = null) {
        fun screen(n: Int): Int {
            return (n + 255) / 2
        }

        val zappyPts = makeZappy(points, 10f, 2.5f, 0.1f)
        drawLineSeq(mat, zappyPts, 5f, 0f, r, g, b, a, null)
        drawLineSeq(mat, zappyPts, 2f, 1f, screen(r), screen(g), screen(b), a, animTime)
    }

    /**
     * Split up a sequence of linePoints with a lightning effect
     * @param hops: rough number of points to subdivide each segment into
     * @param speed: rate at which the lightning effect should move/shake/etc
     */
    @JvmStatic
    fun makeZappy(points: List<Vec2>, hops: Float, variance: Float, speed: Float): List<Vec2> {
        // Nothing in, nothing out
        if (points.isEmpty()) {
            return emptyList()
        }
        val mc = Minecraft.getInstance()
        val zSeed = (mc.frameTime.toDouble() + mc.level!!.levelData.gameTime) * speed
        // Create our output list of zap points
        val zappyPts = mutableListOf(points[0])
        // For each segment in the original...
        for ((i, pair) in points.zipWithNext().withIndex()) {
            val (src, target) = pair
            // Compute distance-squared to the destination, then scale it down by # of hops
            // to know how long each individual hop should be (squared)
            val hopDistSqr = src.distanceToSqr(target) / (hops * hops)
            // Then take the square root to find the actual hop distance
            val hopDist = Mth.sqrt(hopDistSqr)
            // Compute how big the radius of variance should be
            val maxVariance = hopDist * variance

            var position = src
            var j = 0
            while (position.distanceToSqr(target) > hopDistSqr) {
                // Add the next hop...
                val hop = target.add(position.negated()).normalized().scale(hopDist)
                // as well as some random variance...
                // (We use i, j (segment #, subsegment #) as seeds for the Perlin noise,
                // and zSeed (i.e. time elapsed) to perturb the shape gradually over time)
                val theta = (3 * NOISE.getValue(i.toDouble(), j.toDouble(), zSeed) * HexUtils.TAU).toFloat()
                val r = NOISE.getValue(i.inv().toDouble(), j.toDouble(), zSeed).toFloat() * maxVariance
                val randomHop = Vec2(r * Mth.cos(theta), r * Mth.sin(theta))
                position = position.add(hop).add(randomHop)
                // Then record the new location.
                zappyPts.add(position)
                j += 1
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
    @JvmStatic
    fun drawSpot(mat: Matrix4f, point: Vec2, r: Float, g: Float, b: Float, a: Float) {
        val tess = Tesselator.getInstance()
        val buf = tess.builder
        // https://stackoverflow.com/questions/20394727/gl-triangle-strip-vs-gl-triangle-fan
        // Starting point is the center
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR)
        buf.vertex(mat, point.x, point.y, 0f).color(r, g, b, a).endVertex()

        // https://github.com/not-fl3/macroquad/blob/master/src/shapes.rs#L98
        // yes they are gonna be little hexagons fite me
        val fracOfCircle = 6
        val radius = 1.5f
        // run 0 AND last; this way the circle closes
        for (i in 0..fracOfCircle) {
            val theta = i.toFloat() / fracOfCircle * HexUtils.TAU.toFloat()
            val rx = Mth.cos(theta) * radius + point.x
            val ry = Mth.sin(theta) * radius + point.y
            buf.vertex(mat, rx, ry, 0f).color(r, g, b, a).endVertex()
        }

        tess.end()
    }

    @JvmStatic
    fun coordToPx(coord: HexCoord, size: Float, offset: Vec2) =
        Vec2(
            SQRT_3 * coord.q.toFloat() + SQRT_3 / 2.0f * coord.r.toFloat(),
            1.5f * coord.r.toFloat()
        ).scale(size).add(offset)

    @JvmStatic
    fun pxToCoord(px: Vec2, size: Float, offset: Vec2): HexCoord {
        val offsetted = px.add(offset.negated())
        var qf = (SQRT_3 / 3.0f * offsetted.x - 0.33333f * offsetted.y) / size
        var rf = (0.66666f * offsetted.y) / size

        val q = qf.roundToInt()
        val r = rf.roundToInt()
        qf -= q
        rf -= r
        return if (q.absoluteValue >= r.absoluteValue)
            HexCoord(q + (qf + 0.5f * rf).roundToInt(), r)
        else
            HexCoord(q, r + (rf + 0.5 * qf).roundToInt())
    }
}