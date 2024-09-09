package at.petrak.hexcasting.client.gui

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.math.HexAngle
import at.petrak.hexcasting.api.casting.math.HexCoord
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.ShiftScrollListener
import at.petrak.hexcasting.client.ktxt.accumulatedScroll
import at.petrak.hexcasting.client.render.*
import at.petrak.hexcasting.client.sound.GridSoundInstance
import at.petrak.hexcasting.common.lib.HexAttributes
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.msgs.MsgNewSpellPatternC2S
import at.petrak.hexcasting.xplat.IClientXplatAbstractions
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundSource
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec2
import kotlin.math.*

// TODO winfy: fix this class to use ExecutionClientView
class GuiSpellcasting constructor(
    private val handOpenedWith: InteractionHand,
    private var patterns: MutableList<ResolvedPattern>,
    private var cachedStack: List<CompoundTag>,
    private var cachedRavenmind: CompoundTag?,
    private var parenCount: Int,
) : Screen("gui.hexcasting.spellcasting".asTranslatedComponent) {
    private var stackDescs: List<FormattedCharSequence> = listOf()
    private var parenDescs: List<FormattedCharSequence> = listOf()
    private var ravenmind: FormattedCharSequence? = null

    private var drawState: PatternDrawState = PatternDrawState.BetweenPatterns
    private val usedSpots: MutableSet<HexCoord> = HashSet()

    private var ambianceSoundInstance: GridSoundInstance? = null

    private val randSrc = SoundInstance.createUnseededRandom()

    init {
        for ((pattern, origin) in patterns) {
            this.usedSpots.addAll(pattern.positions(origin))
        }
        this.calculateIotaDisplays()
    }

    fun recvServerUpdate(info: ExecutionClientView, index: Int) {
        if (info.isStackClear) {
            this.minecraft?.setScreen(null)
            return
        }

        // TODO this is the kinda hacky bit
        if (info.resolutionType == ResolvedPatternType.UNDONE) {
            this.patterns.reversed().drop(1).firstOrNull { it.type == ResolvedPatternType.ESCAPED }?.let { it.type = ResolvedPatternType.UNDONE }
            this.patterns.getOrNull(index)?.let { it.type = ResolvedPatternType.EVALUATED }
        } else this.patterns.getOrNull(index)?.let {
                it.type = info.resolutionType
            }

        this.cachedStack = info.stackDescs
        this.cachedRavenmind = info.ravenmind
        this.calculateIotaDisplays()
    }

    fun calculateIotaDisplays() {
        val mc = Minecraft.getInstance()
        val width = (this.width * LHS_IOTAS_ALLOCATION).toInt()
        this.stackDescs =
            this.cachedStack.map { IotaType.getDisplayWithMaxWidth(it, width, mc.font) }
                .asReversed()
//        this.parenDescs = if (this.cachedParens.isNotEmpty())
//            this.cachedParens.flatMap { HexIotaTypes.getDisplayWithMaxWidth(it, width, mc.font) }
//        else if (this.parenCount > 0)
//            listOf("...".gold.visualOrderText)
//        else
//            emptyList()
        this.parenDescs = emptyList()
        this.ravenmind =
            this.cachedRavenmind?.let {
                IotaType.getDisplayWithMaxWidth(
                    it,
                    (this.width * RHS_IOTAS_ALLOCATION).toInt(),
                    mc.font
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

        this.calculateIotaDisplays()
    }

    override fun tick() {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player
        if (player != null) {
            val heldItem = player.getItemInHand(handOpenedWith)
            if (heldItem.isEmpty || !heldItem.`is`(HexTags.Items.STAVES))
                closeForReal()
        }
    }

    override fun mouseClicked(mxOut: Double, myOut: Double, pButton: Int): Boolean {
        if (super.mouseClicked(mxOut, myOut, pButton)) {
            return true
        }
        if (HexConfig.client().clickingTogglesDrawing()) {
            return if (this.drawState is PatternDrawState.BetweenPatterns)
                drawStart(mxOut, myOut)
            else
                drawEnd()
        }
        return drawStart(mxOut, myOut)
    }

    private fun drawStart(mxOut: Double, myOut: Double): Boolean {
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
                        randSrc,
                        this.ambianceSoundInstance!!.x,
                        this.ambianceSoundInstance!!.y,
                        this.ambianceSoundInstance!!.z,
                    )
                )
            }
        }

        return false
    }

    override fun mouseMoved(mxOut: Double, myOut: Double) {
        super.mouseMoved(mxOut, myOut)

        if (HexConfig.client().clickingTogglesDrawing() && this.drawState !is PatternDrawState.BetweenPatterns)
            drawMove(mxOut, myOut)
    }

    override fun mouseDragged(mxOut: Double, myOut: Double, pButton: Int, pDragX: Double, pDragY: Double): Boolean {
        if (super.mouseDragged(mxOut, myOut, pButton, pDragX, pDragY)) {
            return true
        }
        if (HexConfig.client().clickingTogglesDrawing())
            return false
        return drawMove(mxOut, myOut)
    }

    private fun drawMove(mxOut: Double, myOut: Double): Boolean {
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
            val snapDist =
                this.hexSize() * this.hexSize() * 2.0 * Mth.clamp(HexConfig.client().gridSnapThreshold(), 0.5, 1.0)
            if (anchor.distanceToSqr(mouse) >= snapDist) {
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
                            HexSounds.ADD_TO_PATTERN,
                            SoundSource.PLAYERS,
                            0.25f,
                            1f + (Math.random().toFloat() - 0.5f) * 0.1f,
                            randSrc,
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
        if (HexConfig.client().clickingTogglesDrawing())
            return false
        return drawEnd()
    }

    private fun drawEnd(): Boolean {
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
                    MsgNewSpellPatternC2S(
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
        if (drawState == PatternDrawState.BetweenPatterns)
            closeForReal()
        else
            drawState = PatternDrawState.BetweenPatterns
    }

    fun closeForReal() {
        Minecraft.getInstance().soundManager.stop(HexSounds.CASTING_AMBIANCE.location, null)

        super.onClose()
    }


    override fun render(graphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick)

        this.ambianceSoundInstance?.mousePosX = pMouseX / this.width.toDouble()
        this.ambianceSoundInstance?.mousePosY = pMouseX / this.width.toDouble()

        val ps = graphics.pose() // TODO: Determine if this is still necessary.

        val mat = ps.last().pose()
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

        for ((idx, elts) in this.patterns.withIndex()) {
            val (pat, origin, valid) = elts
            drawPatternFromPoints(
                mat,
                pat.toLines(
                    this.hexSize(),
                    this.coordToPx(origin)
                ),
                findDupIndices(pat.positions()),
                true,
                valid.color or (0xC8 shl 24),
                valid.fadeColor or (0xC8 shl 24),
                if (valid.success) 0.2f else 0.9f,
                DEFAULT_READABILITY_OFFSET,
                1f,
                idx.toDouble()
            )
        }

        // Now draw the currently WIP pattern
        if (this.drawState !is PatternDrawState.BetweenPatterns) {
            val points = mutableListOf<Vec2>()
            var dupIndices: Set<Int>? = null

            if (this.drawState is PatternDrawState.JustStarted) {
                val ds = this.drawState as PatternDrawState.JustStarted
                points.add(this.coordToPx(ds.start))
            } else if (this.drawState is PatternDrawState.Drawing) {
                val ds = this.drawState as PatternDrawState.Drawing
                dupIndices = findDupIndices(ds.wipPattern.positions())
                for (pos in ds.wipPattern.positions()) {
                    val pix = this.coordToPx(pos + ds.start)
                    points.add(pix)
                }
            }

            points.add(mousePos)
            // Use the size of the patterns as the seed so that way when this one is added the zappies don't jump
            drawPatternFromPoints(mat,
                points,
                dupIndices,
                false,
                0xff_64c8ff_u.toInt(),
                0xff_fecbe6_u.toInt(),
                0.1f,
                DEFAULT_READABILITY_OFFSET,
                1f,
                this.patterns.size.toDouble())
        }

        RenderSystem.enableDepthTest()

        val mc = Minecraft.getInstance()
        val font = mc.font
        ps.pushPose()
        ps.translate(10.0, 10.0, 0.0)

//        if (this.parenCount > 0) {
//            val boxHeight = (this.parenDescs.size + 1f) * 10f
//            RenderSystem.setShader(GameRenderer::getPositionColorShader)
//            RenderSystem.defaultBlendFunc()
//            drawBox(ps, 0f, 0f, (this.width * LHS_IOTAS_ALLOCATION + 5).toFloat(), boxHeight, 7.5f)
//            ps.translate(0.0, 0.0, 1.0)
//
//            val time = ClientTickCounter.getTotal() * 0.16f
//            val opacity = (Mth.map(cos(time), -1f, 1f, 200f, 255f)).toInt()
//            val color = 0x00_ffffff or (opacity shl 24)
//            RenderSystem.setShader { prevShader }
//            for (desc in this.parenDescs) {
//                font.draw(ps, desc, 10f, 7f, color)
//                ps.translate(0.0, 10.0, 0.0)
//            }
//            ps.translate(0.0, 15.0, 0.0)
//        }

        if (this.stackDescs.isNotEmpty()) {
            val boxHeight = (this.stackDescs.size + 1f) * 10f
            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            RenderSystem.enableBlend()
            drawBox(ps, 0f, 0f, (this.width * LHS_IOTAS_ALLOCATION + 5).toFloat(), boxHeight)
            ps.translate(0.0, 0.0, 1.0)
            RenderSystem.setShader { prevShader }
            for (desc in this.stackDescs) {
                graphics.drawString(font, desc, 5, 7, -1) // TODO: Confirm this works
                ps.translate(0.0, 10.0, 0.0)
            }
        }

        ps.popPose()
        if (this.ravenmind != null) {
            val kotlinBad = this.ravenmind!!
            ps.pushPose()
            val boxHeight = 15f
            val addlScale = 1.5f
            ps.translate(this.width * (1.0 - RHS_IOTAS_ALLOCATION * addlScale) - 10, 10.0, 0.0)
            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            RenderSystem.enableBlend()
            drawBox(
                ps, 0f, 0f,
                (this.width * RHS_IOTAS_ALLOCATION * addlScale).toFloat(), boxHeight * addlScale,
            )
            ps.translate(5.0, 5.0, 1.0)
            ps.scale(addlScale, addlScale, 1f)

            val time = ClientTickCounter.getTotal() * 0.2f
            val opacity = (Mth.map(sin(time), -1f, 1f, 150f, 255f)).toInt()
            val color = 0x00_ffffff or (opacity shl 24)

            RenderSystem.setShader { prevShader }
            graphics.drawString(font, kotlinBad, 0, 0, color) // TODO: Confirm this works
            ps.popPose()
        }

        RenderSystem.setShader { prevShader }
    }

    // why the hell is this default true
    override fun isPauseScreen(): Boolean = false

    /** Distance between adjacent hex centers */
    fun hexSize(): Float {
        val scaleModifier = Minecraft.getInstance().player!!.getAttributeValue(HexAttributes.GRID_ZOOM)

        // Originally, we allowed 32 dots across. Assuming a 1920x1080 screen this allowed like 500-odd area.
        // Let's be generous and give them 512.
        val baseScale = sqrt(this.width.toDouble() * this.height / 512.0)
        return (baseScale / scaleModifier).toFloat()
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

    companion object {
        const val LHS_IOTAS_ALLOCATION = 0.7
        const val RHS_IOTAS_ALLOCATION = 0.15

        fun drawBox(ps: PoseStack, x: Float, y: Float, w: Float, h: Float, leftMargin: Float = 2.5f) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            RenderSystem.enableBlend()
            renderQuad(ps, x, y, w, h, 0x50_303030)
            renderQuad(ps, x + leftMargin, y + 2.5f, w - leftMargin - 2.5f, h - 5f, 0x50_303030)
        }
    }
}
