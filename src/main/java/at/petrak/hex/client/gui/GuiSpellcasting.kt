package at.petrak.hex.client.gui

import at.petrak.hex.HexUtils
import at.petrak.hex.HexUtils.TAU
import at.petrak.hex.common.items.ItemSpellbook
import at.petrak.hex.common.network.HexMessages
import at.petrak.hex.common.network.MsgQuitSpellcasting
import at.petrak.hex.common.network.MsgShiftScrollSyn
import at.petrak.hex.hexmath.HexCoord
import at.petrak.hex.hexmath.HexDir
import at.petrak.hex.hexmath.HexPattern
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Matrix4f
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.TextComponent
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import net.minecraft.world.level.levelgen.synth.PerlinNoise
import net.minecraft.world.phys.Vec2
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.roundToInt

const val SQRT_3 = 1.7320508f

class GuiSpellcasting(private val handOpenedWith: InteractionHand) : Screen(TextComponent("")) {
    private var patterns: MutableList<Pair<HexPattern, HexCoord>> = mutableListOf()
    private var drawState: PatternDrawState = PatternDrawState.BetweenPatterns
    private val usedSpots: MutableSet<HexCoord> = HashSet()

    companion object {
        val NOISE = PerlinNoise.create(XoroshiroRandomSource(9001L), listOf(0, 1, 2, 3, 4))
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true
        }

        if (this.drawState is PatternDrawState.BetweenPatterns) {
            val coord = this.pxToCoord(Vec2(pMouseX.toFloat(), pMouseY.toFloat()))
            if (!this.usedSpots.contains(coord)) {
                this.drawState = PatternDrawState.JustStarted(coord)
            }
        }

        return false
    }

    override fun mouseDragged(pMouseX: Double, pMouseY: Double, pButton: Int, pDragX: Double, pDragY: Double): Boolean {
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true
        }

        val anchorCoord = when (this.drawState) {
            PatternDrawState.BetweenPatterns -> null
            is PatternDrawState.JustStarted -> (this.drawState as PatternDrawState.JustStarted).start
            is PatternDrawState.Drawing -> (this.drawState as PatternDrawState.Drawing).current
        }
        if (anchorCoord != null) {
            val anchor = this.coordToPx(anchorCoord)
            val mouse = Vec2(pMouseX.toFloat(), pMouseY.toFloat())
            if (anchor.distanceToSqr(mouse) >= this.hexSize() * this.hexSize()) {
                val delta = mouse.add(anchor.negated())
                val angle = atan2(delta.y, delta.x)
                // 0 is right, increases clockwise(?)
                val snappedAngle = angle.div(TAU.toFloat()).mod(6.0f)
                val newdir = HexDir.values()[(snappedAngle.times(6).roundToInt() + 1).mod(6)]
                // The player might have a lousy aim, so set the new anchor point to the "ideal"
                // location as if they had hit it exactly on the nose.
                val idealNextLoc = anchorCoord + newdir
                if (!this.usedSpots.contains(idealNextLoc)) {
                    if (this.drawState is PatternDrawState.JustStarted) {
                        val pat = HexPattern(newdir)

                        this.drawState = PatternDrawState.Drawing(anchorCoord, idealNextLoc, pat)
                    } else if (this.drawState is PatternDrawState.Drawing) {
                        // how anyone gets around without a borrowck is beyond me
                        val ds = (this.drawState as PatternDrawState.Drawing)
                        val success = ds.wipPattern.tryAppendDir(newdir)
                        if (success) {
                            ds.current = idealNextLoc
                        }
                    }
                }
            }
        }

        return false
    }

    override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (super.mouseReleased(pMouseX, pMouseY, pButton)) {
            return true
        }

        when (this.drawState) {
            PatternDrawState.BetweenPatterns -> {}
            is PatternDrawState.JustStarted -> {
                // Well, we never managed to get anything on the stack this go-around.
                this.drawState = PatternDrawState.BetweenPatterns
                if (this.patterns.isEmpty()) {
                    Minecraft.getInstance().setScreen(null)
                }
            }
            is PatternDrawState.Drawing -> {
                val (start, _, pat) = this.drawState as PatternDrawState.Drawing
                this.drawState = PatternDrawState.BetweenPatterns
                this.patterns.add(Pair(pat, start))

                this.usedSpots.addAll(pat.positions(start))

                HexMessages.getNetwork().sendToServer(
                    at.petrak.hex.common.network.MsgNewSpellPatternSyn(
                        this.handOpenedWith,
                        pat
                    )
                )
            }
        }

        return false
    }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        super.mouseScrolled(pMouseX, pMouseY, pDelta)

        val otherHand = HexUtils.OtherHand(this.handOpenedWith)
        if (Minecraft.getInstance().player!!.getItemInHand(otherHand).item is ItemSpellbook)
            HexMessages.getNetwork().sendToServer(MsgShiftScrollSyn(otherHand, pDelta))

        return true
    }

    override fun onClose() {
        HexMessages.getNetwork().sendToServer(MsgQuitSpellcasting())

        super.onClose()
    }

    override fun render(poseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTick)

        // Split up a sequence of lines with a lightning effect
        // hops: rough number of points to subdivide each segment into
        // speed: rate at which the lightning effect should move/shake/etc
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
                    val theta = (3 * NOISE.getValue(i.toDouble(), j.toDouble(), zSeed) * TAU).toFloat()
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

        fun drawLineSeq(mat: Matrix4f, points: List<Vec2>, width: Float, z: Float, r: Int, g: Int, b: Int, a: Int) {
            // they spell it wrong at mojang lmao
            val tess = Tesselator.getInstance()
            val buf = tess.builder

            // We use one single TRIANGLE_STRIP
            // in order to connect adjacent segments together and not get the weird hinge effect.
            // There's still some artifacting but this is passable, at least.
            buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR)

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

                buf.vertex(mat, p1.x + tx, p1.y + ty, z).color(r, g, b, a).endVertex()
                buf.vertex(mat, p1.x - tx, p1.y - ty, z).color(r, g, b, a).endVertex()
                buf.vertex(mat, p2.x + tx, p2.y + ty, z).color(r, g, b, a).endVertex()
                buf.vertex(mat, p2.x - tx, p2.y - ty, z).color(r, g, b, a).endVertex()
            }

            tess.end()
        }

        fun drawPattern(mat: Matrix4f, points: List<Vec2>, r: Int, g: Int, b: Int, a: Int) {
            fun screen(n: Int): Int {
                return (n + 255) / 2
            }

            val zappyPts = makeZappy(points, 10f, 2.5f, 0.1f)
            drawLineSeq(mat, zappyPts, 5f, 0f, r, g, b, a)
            drawLineSeq(mat, zappyPts, 2f, 1f, screen(r), screen(g), screen(b), a)
        }

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
            for (i in 0..32) {
                val theta = i.toFloat() / fracOfCircle * TAU.toFloat()
                val rx = Mth.cos(theta) * radius + point.x
                val ry = Mth.sin(theta) * radius + point.y
                buf.vertex(mat, rx, ry, 0f).color(r, g, b, a).endVertex()
            }

            tess.end()
        }

        val mat = poseStack.last().pose()
        val prevShader = RenderSystem.getShader()
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()

        // Draw guide dots around the cursor
        val mousePos = Vec2(pMouseX.toFloat(), pMouseY.toFloat())
        // snap it to the center
        val mouseCoord = this.pxToCoord(mousePos)
        val radius = 3
        for (dotCoord in mouseCoord.rangeAround(radius)) {
            if (!this.usedSpots.contains(dotCoord)) {
                val dotPx = this.coordToPx(dotCoord)
                val delta = dotPx.add(mousePos.negated()).length()
                // when right on top of the cursor, 1.0
                // when at the full radius, 0! this is so we don't have dots suddenly appear/disappear.
                // we subtract size from delta so there's a little "island" of 100% bright points by the mouse
                val scaledDist = Mth.clamp(
                    1.0f - ((delta - this.hexSize()) / (radius.toFloat() * this.hexSize())),
                    0f,
                    1f
                )
                drawSpot(
                    mat,
                    dotPx,
                    Mth.lerp(scaledDist, 0.4f, 0.5f),
                    Mth.lerp(scaledDist, 0.8f, 1.0f),
                    Mth.lerp(scaledDist, 0.7f, 0.9f),
                    scaledDist
                )
            }
        }
        RenderSystem.defaultBlendFunc()

        for ((pat, origin) in this.patterns) {
            drawPattern(mat, pat.positions(origin).map(this::coordToPx), 127, 127, 255, 200)
        }

        // Now draw the currently WIP pattern
        if (this.drawState !is PatternDrawState.BetweenPatterns) {
            val points = mutableListOf<Vec2>()

            if (this.drawState is PatternDrawState.JustStarted) {
                val ds = this.drawState as PatternDrawState.JustStarted
                points.add(this.coordToPx(ds.start))
            } else if (this.drawState is PatternDrawState.Drawing) {
                val ds = this.drawState as PatternDrawState.Drawing
                for (pos in ds.wipPattern.positions()) {
                    val pix = this.coordToPx(pos + ds.start)
                    points.add(pix)
                }
            }

            points.add(mousePos)
            drawPattern(mat, points, 100, 200, 255, 255)
        }

        RenderSystem.setShader { prevShader }
        RenderSystem.enableDepthTest()
    }

    // why the hell is this default true
    override fun isPauseScreen(): Boolean = false

    /** Distance between adjacent hex centers */
    fun hexSize(): Float = this.width.toFloat() / 32.0f
    fun coordsOffset(): Vec2 = Vec2(0f, this.hexSize())

    fun coordToPx(coord: HexCoord) =
        Vec2(
            SQRT_3 * coord.q.toFloat() + SQRT_3 / 2.0f * coord.r.toFloat(),
            1.5f * coord.r.toFloat()
        ).scale(this.hexSize()).add(this.coordsOffset())

    fun pxToCoord(px: Vec2): HexCoord {
        val offsetted = px.add(this.coordsOffset().negated())
        var qf = (SQRT_3 / 3.0f * offsetted.x - 0.33333f * offsetted.y) / hexSize()
        var rf = (0.66666f * offsetted.y) / hexSize()

        val q = qf.roundToInt()
        val r = rf.roundToInt()
        qf -= q
        rf -= r
        return if (q.absoluteValue >= r.absoluteValue)
            HexCoord(q + (qf + 0.5f * rf).roundToInt(), r)
        else
            HexCoord(q, r + (rf + 0.5 * qf).roundToInt())
    }

    private sealed class PatternDrawState {
        /** We're waiting on the player to right-click again */
        object BetweenPatterns : PatternDrawState()

        /** We just started drawing and haven't drawn the first line yet. */
        data class JustStarted(val start: HexCoord) : PatternDrawState()

        /** We've started drawing a pattern for real. */
        data class Drawing(val start: HexCoord, var current: HexCoord, val wipPattern: HexPattern) : PatternDrawState()
    }
}
