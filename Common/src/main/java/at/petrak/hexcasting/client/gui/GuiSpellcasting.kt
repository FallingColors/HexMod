package at.petrak.hexcasting.client.gui

import at.petrak.hexcasting.api.misc.DiscoveryHandlers
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexItemTags
import at.petrak.hexcasting.api.spell.casting.ControllerInfo
import at.petrak.hexcasting.api.spell.casting.ResolvedPattern
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import at.petrak.hexcasting.api.spell.math.HexAngle
import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.client.ShiftScrollListener
import at.petrak.hexcasting.client.drawPatternFromPoints
import at.petrak.hexcasting.client.drawSpot
import at.petrak.hexcasting.client.ktxt.accumulatedScroll
import at.petrak.hexcasting.client.sound.GridSoundInstance
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.network.MsgNewSpellPatternSyn
import at.petrak.hexcasting.xplat.IClientXplatAbstractions
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec2
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

class GuiSpellcasting(
    private val handOpenedWith: InteractionHand,
    private var patterns: MutableList<ResolvedPattern>,
    private var stackDescs: List<Component>
) : Screen("gui.hexcasting.spellcasting".asTranslatedComponent) {
    private var drawState: PatternDrawState = PatternDrawState.BetweenPatterns
    private val usedSpots: MutableSet<HexCoord> = HashSet()

    private var ambianceSoundInstance: GridSoundInstance? = null

    init {
        for ((pattern, origin) in patterns) {
            this.usedSpots.addAll(pattern.positions(origin))
        }
    }

    fun recvServerUpdate(info: ControllerInfo, index: Int) {
        this.stackDescs = info.stackDesc
        this.patterns.getOrNull(index)?.let {
            it.type = info.resolutionType
        }

        if (info.resolutionType.success) {
            Minecraft.getInstance().soundManager.play(
                SimpleSoundInstance(
                    HexSounds.ADD_PATTERN,
                    SoundSource.PLAYERS,
                    0.5f,
                    1f + (Math.random().toFloat() - 0.5f) * 0.1f,
                    this.ambianceSoundInstance!!.x,
                    this.ambianceSoundInstance!!.y,
                    this.ambianceSoundInstance!!.z,
                )
            )
        }
    }

    override fun init() {
        val minecraft = Minecraft.getInstance()
        val soundManager = minecraft.soundManager
        soundManager.stop(HexSounds.CASTING_AMBIANCE.location, null)
        val player = minecraft.player
        if (player != null) {
            this.ambianceSoundInstance = GridSoundInstance(player)
            soundManager.play(this.ambianceSoundInstance!!)
        }
    }

    override fun tick() {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player
        if (player != null) {
            val heldItem = player.getItemInHand(handOpenedWith)
            if (heldItem.isEmpty || !heldItem.`is`(HexItemTags.WANDS))
                onClose()
        }
    }

    override fun mouseClicked(mxOut: Double, myOut: Double, pButton: Int): Boolean {
        if (super.mouseClicked(mxOut, myOut, pButton)) {
            return true
        }

        val mx = Mth.clamp(mxOut, 0.0, this.width.toDouble())
        val my = Mth.clamp(myOut, 0.0, this.height.toDouble())
        if (this.drawState is PatternDrawState.BetweenPatterns) {
            val coord = this.pxToCoord(Vec2(mx.toFloat(), my.toFloat()))
            if (!this.usedSpots.contains(coord)) {
                this.drawState = PatternDrawState.JustStarted(coord)
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance(
                        HexSounds.START_PATTERN,
                        SoundSource.PLAYERS,
                        0.25f,
                        1f,
                        this.ambianceSoundInstance!!.x,
                        this.ambianceSoundInstance!!.y,
                        this.ambianceSoundInstance!!.z,
                    )
                )
            }
        }

        return false
    }

    override fun mouseDragged(mxOut: Double, myOut: Double, pButton: Int, pDragX: Double, pDragY: Double): Boolean {
        if (super.mouseDragged(mxOut, myOut, pButton, pDragX, pDragY)) {
            return true
        }

        val mx = Mth.clamp(mxOut, 0.0, this.width.toDouble())
        val my = Mth.clamp(myOut, 0.0, this.height.toDouble())

        val anchorCoord = when (this.drawState) {
            PatternDrawState.BetweenPatterns -> null
            is PatternDrawState.JustStarted -> (this.drawState as PatternDrawState.JustStarted).start
            is PatternDrawState.Drawing -> (this.drawState as PatternDrawState.Drawing).current
        }
        if (anchorCoord != null) {
            val anchor = this.coordToPx(anchorCoord)
            val mouse = Vec2(mx.toFloat(), my.toFloat())
            if (anchor.distanceToSqr(mouse) >=
                this.hexSize() * this.hexSize() * 2.0 * HexConfig.client().gridSnapThreshold()
            ) {
                val delta = mouse.add(anchor.negated())
                val angle = atan2(delta.y, delta.x)
                // 0 is right, increases clockwise(?)
                val snappedAngle = angle.div(Mth.TWO_PI).mod(6.0f)
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
                        SimpleSoundInstance(
                            HexSounds.ADD_LINE,
                            SoundSource.PLAYERS,
                            0.25f,
                            1f + (Math.random().toFloat() - 0.5f) * 0.1f,
                            this.ambianceSoundInstance!!.x,
                            this.ambianceSoundInstance!!.y,
                            this.ambianceSoundInstance!!.z,
                        )
                    )
                }
            }
        }

        return false
    }

    override fun mouseReleased(mx: Double, my: Double, pButton: Int): Boolean {
        if (super.mouseReleased(mx, my, pButton)) {
            return true
        }

        when (this.drawState) {
            PatternDrawState.BetweenPatterns -> {}
            is PatternDrawState.JustStarted -> {
                // Well, we never managed to get anything on the stack this go-around.
                this.drawState = PatternDrawState.BetweenPatterns
            }
            is PatternDrawState.Drawing -> {
                val (start, _, pat) = this.drawState as PatternDrawState.Drawing
                this.drawState = PatternDrawState.BetweenPatterns
                this.patterns.add(ResolvedPattern(pat, start, ResolvedPatternType.UNRESOLVED))

                this.usedSpots.addAll(pat.positions(start))

                IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                    MsgNewSpellPatternSyn(
                        this.handOpenedWith,
                        pat,
                        this.patterns
                    )
                )
            }
        }

        return false
    }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        super.mouseScrolled(pMouseX, pMouseY, pDelta)

        val mouseHandler = Minecraft.getInstance().mouseHandler

        if (mouseHandler.accumulatedScroll != 0.0 && sign(pDelta) != sign(mouseHandler.accumulatedScroll)) {
            mouseHandler.accumulatedScroll = 0.0
        }

        mouseHandler.accumulatedScroll += pDelta
        val accumulation: Int = mouseHandler.accumulatedScroll.toInt()
        if (accumulation == 0) {
            return true
        }

        mouseHandler.accumulatedScroll -= accumulation.toDouble()

        ShiftScrollListener.onScroll(pDelta, false)

        return true
    }

    override fun onClose() {
        Minecraft.getInstance().soundManager.stop(HexSounds.CASTING_AMBIANCE.location, null)

        super.onClose()
    }

    override fun render(poseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTick)

        this.ambianceSoundInstance?.mousePosX = pMouseX / this.width.toDouble()
        this.ambianceSoundInstance?.mousePosY = pMouseX / this.width.toDouble()

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
                    scaledDist * 2f,
                    Mth.lerp(scaledDist, 0.4f, 0.5f),
                    Mth.lerp(scaledDist, 0.8f, 1.0f),
                    Mth.lerp(scaledDist, 0.7f, 0.9f),
                    scaledDist
                )
            }
        }
        RenderSystem.defaultBlendFunc()

        for ((pat, origin, valid) in this.patterns) {
            drawPatternFromPoints(
                mat,
                pat.toLines(
                    this.hexSize(),
                    this.coordToPx(origin)
                ),
                true,
                valid.color or (0xC8 shl 24),
                valid.fadeColor or (0xC8 shl 24),
                if (valid.success) 0.2f else 0.9f
            )
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
            drawPatternFromPoints(mat, points, false, 0xff_64c8ff_u.toInt(), 0xff_fecbe6_u.toInt(), 0.1f)
        }

        RenderSystem.setShader { prevShader }
        RenderSystem.enableDepthTest()

        val mc = Minecraft.getInstance()
        val font = mc.font
        for ((i, s) in this.stackDescs.withIndex()) {
            val offsetIdx = this.stackDescs.size - i - 1
            font.draw(poseStack, s, 10f, 10f + 9f * offsetIdx, -1)
        }
    }

    // why the hell is this default true
    override fun isPauseScreen(): Boolean = false

    /** Distance between adjacent hex centers */
    fun hexSize(): Float {
        val scaleModifier = DiscoveryHandlers.gridScaleModifier(Minecraft.getInstance().player)

        // Originally, we allowed 32 dots across. Assuming a 1920x1080 screen this allowed like 500-odd area.
        // Let's be generous and give them 512.
        val baseScale = sqrt(this.width.toDouble() * this.height / 512.0)
        return baseScale.toFloat() * scaleModifier
    }

    fun coordsOffset(): Vec2 = Vec2(this.width.toFloat() * 0.5f, this.height.toFloat() * 0.5f)

    fun coordToPx(coord: HexCoord) =
        at.petrak.hexcasting.api.utils.coordToPx(coord, this.hexSize(), this.coordsOffset())

    fun pxToCoord(px: Vec2) = at.petrak.hexcasting.api.utils.pxToCoord(px, this.hexSize(), this.coordsOffset())


    private sealed class PatternDrawState {
        /** We're waiting on the player to right-click again */
        object BetweenPatterns : PatternDrawState()

        /** We just started drawing and haven't drawn the first line yet. */
        data class JustStarted(val start: HexCoord) : PatternDrawState()

        /** We've started drawing a pattern for real. */
        data class Drawing(val start: HexCoord, var current: HexCoord, val wipPattern: HexPattern) : PatternDrawState()
    }
}
