package at.petrak.hex.client.gui

import at.petrak.hex.HexMod
import at.petrak.hex.HexUtils.TAU
import at.petrak.hex.hexes.HexCoord
import at.petrak.hex.hexes.HexDir
import at.petrak.hex.hexes.HexPattern
import at.petrak.hex.network.HexMessages
import at.petrak.hex.network.MsgNewSpellPatternSyn
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.phys.Vec2
import kotlin.math.*

class GuiSpellcasting : Screen(TextComponent("")) {
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
                HexMod.LOGGER.info("($pMouseX, $pMouseY) was sufficiently far from (${anchor.x}, ${anchor.y})")
                val delta = mouse.add(anchor.negated())
                val angle = atan2(delta.y, delta.x)
                // 0 is right, increases clockwise(?)
                val snappedAngle = angle.div(TAU.toFloat()).mod(6.0f)
                val newdir = HexDir.values()[(-snappedAngle.times(6).roundToInt() + 1).mod(6)]
                // The player might have a lousy aim, so set the new anchor point to the "ideal"
                // location as if they had hit it exactly on the nose.
                val idealNextLoc = anchor.add(Vec2(cos(snappedAngle), sin(snappedAngle)))

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
                        HexMod.LOGGER.info("Added to pattern: ${ds.wipPattern} ; New current pos: (${ds.current.x}, ${ds.current.y})")
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
                val (tstart, current, pat) = this.drawState as PatternDrawState.Drawing
                this.drawState = PatternDrawState.BetweenPatterns

                HexMessages.getNetwork().sendToServer(MsgNewSpellPatternSyn(0, pat))
            }
        }

        return false
    }

    override fun render(poseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTick)

        // they spell it wrong at mojang lmao
        val tess = Tesselator.getInstance()
        val buf = tess.builder

        RenderSystem.disableCull()
        RenderSystem.disableTexture()
        RenderSystem.enableBlend()
        for ((pat, origin) in this.patterns) {
            buf.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR)
            for (coord in pat.positions()) {
                val pix = this.coordToPx(coord, origin)
                buf.vertex(pix.x.toDouble(), pix.y.toDouble(), 0.0).color(127, 127, 255, 255).endVertex()
            }
            tess.end()
        }

        // Now draw the currently WIP pattern
        if (this.drawState !is PatternDrawState.BetweenPatterns) {
            buf.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR)

            if (this.drawState is PatternDrawState.JustStarted) {
                val ds = this.drawState as PatternDrawState.JustStarted
                buf.vertex(ds.start.x.toDouble(), ds.start.y.toDouble(), 0.0).color(200, 200, 255, 255).endVertex()
            } else if (this.drawState is PatternDrawState.Drawing) {
                val ds = this.drawState as PatternDrawState.Drawing
                for (pos in ds.wipPattern.positions()) {
                    val pix = this.coordToPx(pos, ds.start)
                    buf.vertex(pix.x.toDouble(), pix.y.toDouble(), 0.0).color(200, 200, 255, 255).endVertex()
                }
            }

            buf.vertex(pMouseX.toDouble(), pMouseY.toDouble(), 0.0).color(240, 240, 255, 255).endVertex()
            tess.end()
        }
    }

    // why the hell is this default true
    override fun isPauseScreen(): Boolean = false

    /** Distance between adjacent hex centers */
    fun hexSize(): Float =
        this.width.toFloat() / 16.0f

    fun coordToPx(coord: HexCoord, origin: Vec2) =
        origin.add(
            Vec2(
                sqrt(3.0f) * coord.q.toFloat() + sqrt(3.0f) / 2.0f * coord.r.toFloat(),
                -1.5f * coord.r
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