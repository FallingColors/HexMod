package at.petrak.hex.client.gui

import at.petrak.hex.HexMod
import at.petrak.hex.HexUtils.TAU
import at.petrak.hex.hexmath.HexAngle
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
import net.minecraft.world.phys.Vec2
import kotlin.math.atan2
import kotlin.math.roundToInt

const val SQRT_3 = 1.7320508f

class GuiSpellcasting(private val handOpenedWith: InteractionHand) : Screen(TextComponent("")) {
    private var patterns: MutableList<Pair<HexPattern, Vec2>> = mutableListOf()
    private var drawState: PatternDrawState = PatternDrawState.BetweenPatterns

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true
        }

        if (this.drawState is PatternDrawState.BetweenPatterns) {
            this.drawState = PatternDrawState.JustStarted(Vec2(pMouseX.toFloat(), pMouseY.toFloat()))
        }

        return false
    }

    override fun mouseDragged(pMouseX: Double, pMouseY: Double, pButton: Int, pDragX: Double, pDragY: Double): Boolean {
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true
        }

        val anchor = when (this.drawState) {
            PatternDrawState.BetweenPatterns -> null
            is PatternDrawState.JustStarted -> (this.drawState as PatternDrawState.JustStarted).start
            is PatternDrawState.Drawing -> (this.drawState as PatternDrawState.Drawing).current
        }
        if (anchor != null) {
            val mouse = Vec2(pMouseX.toFloat(), pMouseY.toFloat())
            if (anchor.distanceToSqr(mouse) >= this.hexSize() * this.hexSize()) {
                val delta = mouse.add(anchor.negated())
                val angle = atan2(delta.y, delta.x)
                // 0 is right, increases clockwise(?)
                val snappedAngle = angle.div(TAU.toFloat()).mod(6.0f)
                val newdir = HexDir.values()[(snappedAngle.times(6).roundToInt() + 1).mod(6)]
                // The player might have a lousy aim, so set the new anchor point to the "ideal"
                // location as if they had hit it exactly on the nose.
                val idealNextLoc = this.coordToPx(newdir.asDelta(), anchor)

                if (this.drawState is PatternDrawState.JustStarted) {
                    val pat = HexPattern(newdir)

                    this.drawState = PatternDrawState.Drawing(anchor, idealNextLoc, pat)
                    HexMod.LOGGER.info("Started drawing new pattern: $pat")
                } else if (this.drawState is PatternDrawState.Drawing) {
                    // how anyone gets around without a borrowck is beyond me
                    val ds = (this.drawState as PatternDrawState.Drawing)
                    val success = ds.wipPattern.tryAppendDir(newdir)
                    if (success) {
                        ds.current = idealNextLoc
                        HexMod.LOGGER.info("Added to pattern: ${ds.wipPattern}")
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

                at.petrak.hex.common.network.HexMessages.getNetwork().sendToServer(
                    at.petrak.hex.common.network.MsgNewSpellPatternSyn(
                        this.handOpenedWith,
                        pat
                    )
                )
            }
        }

        return false
    }

    override fun onClose() {
        at.petrak.hex.common.network.HexMessages.getNetwork()
            .sendToServer(at.petrak.hex.common.network.MsgQuitSpellcasting())

        super.onClose()
    }

    override fun render(poseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTick)

        fun drawLineSeq(mat: Matrix4f, points: List<Vec2>, r: Int, g: Int, b: Int, a: Int) {
            // they spell it wrong at mojang lmao
            val tess = Tesselator.getInstance()
            val buf = tess.builder

            for ((p1, p2) in points.zipWithNext()) {
                // https://github.com/not-fl3/macroquad/blob/master/src/shapes.rs#L163
                // GuiComponent::innerFill line 52
                // fedor have useful variable names challenge (99% can't beat)
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                // normal x and y, presumably?
                val nx = -dy
                val ny = dx
                val width = 1.5f
                // thickness?
                val tlen = Mth.sqrt(nx * nx + ny * ny) / (width * 0.5f)
                val tx = nx / tlen
                val ty = ny / tlen

                buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
                buf.vertex(mat, p1.x + tx, p1.y + ty, 0f).color(r, g, b, a).endVertex()
                buf.vertex(mat, p2.x + tx, p2.y + ty, 0f).color(r, g, b, a).endVertex()
                buf.vertex(mat, p2.x - tx, p2.y - ty, 0f).color(r, g, b, a).endVertex()
                buf.vertex(mat, p1.x - tx, p1.y - ty, 0f).color(r, g, b, a).endVertex()

                tess.end()
            }
        }

        fun drawSpot(mat: Matrix4f, point: Vec2, r: Int, g: Int, b: Int, a: Int) {
            val tess = Tesselator.getInstance()
            val buf = tess.builder
            // https://stackoverflow.com/questions/20394727/gl-triangle-strip-vs-gl-triangle-fan
            // Starting point is the center
            buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR)
            buf.vertex(mat, point.x, point.y, 0f).color(r, g, b, a).endVertex()

            // https://github.com/not-fl3/macroquad/blob/master/src/shapes.rs#L98
            val fracOfCircle = 32
            val radius = 1.0f
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

        for ((pat, origin) in this.patterns) {
            drawLineSeq(mat, pat.positions().map { pos -> this.coordToPx(pos, origin) }, 127, 127, 255, 200)
        }

        // Now draw the currently WIP pattern
        if (this.drawState !is PatternDrawState.BetweenPatterns) {
            val points = mutableListOf<Vec2>()

            val (dirs, spotAnchor) = if (this.drawState is PatternDrawState.JustStarted) {
                val ds = this.drawState as PatternDrawState.JustStarted
                points.add(ds.start)
                Pair(HexDir.values().toList(), ds.start)
            } else if (this.drawState is PatternDrawState.Drawing) {
                val ds = this.drawState as PatternDrawState.Drawing
                for (pos in ds.wipPattern.positions()) {
                    val pix = this.coordToPx(pos, ds.start)
                    points.add(pix)
                }
                val finalDir = ds.wipPattern.finalDir()
                Pair(
                    HexAngle.values().flatMap {
                        if (it == HexAngle.BACK) {
                            emptyList()
                        } else {
                            listOf(finalDir * it)
                        }
                    },
                    ds.current
                )
            } else {
                throw NotImplementedError("unreachable")
            }

            points.add(Vec2(pMouseX.toFloat(), pMouseY.toFloat()))
            drawLineSeq(mat, points, 200, 200, 255, 255)

            for (dir in dirs) {
                val pos = this.coordToPx(dir.asDelta(), spotAnchor)
                drawSpot(mat, pos, 200, 200, 230, 255)
            }
        }

        RenderSystem.setShader { prevShader }
        RenderSystem.enableDepthTest()
    }

    // why the hell is this default true
    override fun isPauseScreen(): Boolean = false

    /** Distance between adjacent hex centers */
    fun hexSize(): Float =
        this.width.toFloat() / 32.0f


    fun coordToPx(coord: HexCoord, origin: Vec2) =
        origin.add(
            Vec2(
                SQRT_3 * coord.q.toFloat() + SQRT_3 / 2.0f * coord.r.toFloat(),
                1.5f * coord.r.toFloat()
            ).scale(this.hexSize())
        )

    private sealed class PatternDrawState {
        /** We're waiting on the player to right-click again */
        object BetweenPatterns : PatternDrawState()

        /** We just started drawing and haven't drawn the first line yet. */
        data class JustStarted(val start: Vec2) : PatternDrawState()

        /** We've started drawing a pattern for real. */
        data class Drawing(val start: Vec2, var current: Vec2, val wipPattern: HexPattern) : PatternDrawState()
    }
}