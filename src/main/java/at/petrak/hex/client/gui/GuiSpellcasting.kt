package at.petrak.hex.client.gui

import at.petrak.hex.HexUtils
import at.petrak.hex.HexUtils.TAU
import at.petrak.hex.client.RenderLib
import at.petrak.hex.common.items.HexItems
import at.petrak.hex.common.items.ItemSpellbook
import at.petrak.hex.common.lib.HexSounds
import at.petrak.hex.common.network.HexMessages
import at.petrak.hex.common.network.MsgQuitSpellcasting
import at.petrak.hex.common.network.MsgShiftScrollSyn
import at.petrak.hex.hexmath.HexAngle
import at.petrak.hex.hexmath.HexCoord
import at.petrak.hex.hexmath.HexDir
import at.petrak.hex.hexmath.HexPattern
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.resources.sounds.AbstractSoundInstance
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.network.chat.TextComponent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec2
import kotlin.math.atan2
import kotlin.math.roundToInt

const val SQRT_3 = 1.7320508f

class GuiSpellcasting(private val handOpenedWith: InteractionHand) : Screen(TextComponent("")) {
    private var patterns: MutableList<Pair<HexPattern, HexCoord>> = mutableListOf()
    private var drawState: PatternDrawState = PatternDrawState.BetweenPatterns
    private val usedSpots: MutableSet<HexCoord> = HashSet()

    private var ambianceSoundInstance: AbstractSoundInstance? = null

    var stackDescs: List<String> = emptyList()

    override fun init() {
        this.ambianceSoundInstance = SimpleSoundInstance(
            HexSounds.CASTING_AMBIANCE.get().location,
            SoundSource.PLAYERS,
            1f,
            1f,
            true,
            0,
            SoundInstance.Attenuation.NONE,
            0.0,
            0.0,
            0.0,
            true // this means is it relative to the *player's ears*, not to a given point, thanks mojank
        )
        Minecraft.getInstance().soundManager.play(this.ambianceSoundInstance!!)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true
        }

        if (this.drawState is PatternDrawState.BetweenPatterns) {
            val coord = this.pxToCoord(Vec2(pMouseX.toFloat(), pMouseY.toFloat()))
            if (!this.usedSpots.contains(coord)) {
                this.drawState = PatternDrawState.JustStarted(coord)
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance.forUI(
                        HexSounds.START_PATTERN.get(),
                        1f
                    )
                )
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
                var playSound = false
                if (!this.usedSpots.contains(idealNextLoc)) {
                    if (this.drawState is PatternDrawState.JustStarted) {
                        val pat = HexPattern(newdir)

                        this.drawState = PatternDrawState.Drawing(anchorCoord, idealNextLoc, pat)
                        playSound = true
                    } else if (this.drawState is PatternDrawState.Drawing) {
                        // how anyone gets around without a borrowck is beyond me
                        val ds = (this.drawState as PatternDrawState.Drawing)
                        val lastDir = ds.wipPattern.finalDir()
                        if (newdir == lastDir.rotatedBy(HexAngle.BACK)) {
                            // We're diametrically opposite! Do a backtrack
                            if (ds.wipPattern.angles.isEmpty()) {
                                this.drawState = PatternDrawState.JustStarted(ds.current + newdir)
                            } else {
                                ds.current += newdir
                                ds.wipPattern.angles.removeLast()
                            }
                            playSound = true
                        } else {
                            val success = ds.wipPattern.tryAppendDir(newdir)
                            if (success) {
                                ds.current = idealNextLoc
                            }
                            playSound = success
                        }
                    }
                }

                if (playSound) {
                    Minecraft.getInstance().soundManager.play(
                        SimpleSoundInstance.forUI(
                            HexSounds.ADD_LINE.get(),
                            1f + (Math.random().toFloat() - 0.5f) * 0.1f
                        )
                    )
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
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance.forUI(
                        HexSounds.ADD_PATTERN.get(),
                        1f + (Math.random().toFloat() - 0.5f) * 0.1f
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

        this.ambianceSoundInstance?.let { Minecraft.getInstance().soundManager.stop(it) }

        super.onClose()
    }

    override fun render(poseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTick)

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
                // TODO: this doesn't work great around the upper-right part of the screen
                val scaledDist = Mth.clamp(
                    1.0f - ((delta - this.hexSize()) / (radius.toFloat() * this.hexSize())),
                    0f,
                    1f
                )
                RenderLib.drawSpot(
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

        val alreadyPats = this.patterns.map { (pat, origin) ->
            pat.toLines(
                this.hexSize(),
                this.coordToPx(origin)
            )
        }
        for (pat in alreadyPats) {
            RenderLib.drawPattern(mat, pat, 127, 127, 255, 200)
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
            RenderLib.drawPattern(mat, points, 100, 200, 255, 255)
        }

        RenderSystem.setShader { prevShader }
        RenderSystem.enableDepthTest()

        val mc = Minecraft.getInstance()
        if (mc.player?.getItemInHand(HexUtils.OtherHand(handOpenedWith))?.`is`(HexItems.SCRYING_LENS.get()) == true) {

            val font = mc.font
            for ((i, s) in this.stackDescs.withIndex()) {
                val offsetIdx = this.stackDescs.size - i - 1
                font.draw(poseStack, s, 10f, 10f + 9f * offsetIdx, -1)
            }
        }
    }

    // why the hell is this default true
    override fun isPauseScreen(): Boolean = false

    /** Distance between adjacent hex centers */
    fun hexSize(): Float = this.width.toFloat() / 32.0f
    fun coordsOffset(): Vec2 = Vec2(
        (this.width / 2) % hexSize(),
        (this.height / 2) % hexSize(),
    ).negated()

    fun coordToPx(coord: HexCoord) = RenderLib.coordToPx(coord, this.hexSize(), this.coordsOffset())
    fun pxToCoord(px: Vec2) = RenderLib.pxToCoord(px, this.hexSize(), this.coordsOffset())


    private sealed class PatternDrawState {
        /** We're waiting on the player to right-click again */
        object BetweenPatterns : PatternDrawState()

        /** We just started drawing and haven't drawn the first line yet. */
        data class JustStarted(val start: HexCoord) : PatternDrawState()

        /** We've started drawing a pattern for real. */
        data class Drawing(val start: HexCoord, var current: HexCoord, val wipPattern: HexPattern) : PatternDrawState()
    }
}
