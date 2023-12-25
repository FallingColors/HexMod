@file:JvmName("ClientRenderHelper")
package at.petrak.hexcasting.api.client

import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.render.drawLineSeq
import at.petrak.hexcasting.client.render.findDupIndices
import at.petrak.hexcasting.client.render.makeZappy
import at.petrak.hexcasting.client.render.screenCol
import at.petrak.hexcasting.xplat.IClientXplatAbstractions
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


fun renderCastingStack(ps: PoseStack, player: Player, pticks: Float) {
    val stack = IClientXplatAbstractions.INSTANCE.getClientCastingStack(player)

    for (k in 0 until stack.getPatterns().size) {
        val patternRenderHolder = stack.getPatternHolder(k) ?: continue
        val pattern = patternRenderHolder.pattern
        val lifetime = patternRenderHolder.lifetime
        val lifetimeOffset = if (lifetime <= 5f) (5f - lifetime) / 5f else 0f

        ps.pushPose()
        ps.mulPose(Axis.YP.rotationDegrees(((player.level().gameTime + pticks) * (sin(k * 12.543565f) * 3.4f) * (k / 12.43f) % 360 + (1 + k) * 45f)))
        ps.translate(0.0, 1 + sin(k.toDouble()) * 0.75, 0.75 + cos((k / 8.0)) * 0.25 + cos((player.level().gameTime + pticks) / (7 + k / 4)) * 0.065)
        ps.scale(1 / 24f * (1 - lifetimeOffset), 1 / 24f * (1 - lifetimeOffset), 1 / 24f * (1 - lifetimeOffset))
        ps.translate(0.0, floor((k / 8.0)), 0.0)
        ps.translate(0.0, sin((player.level().gameTime + pticks) / (7.0 + k / 8.0)), 0.0)

        val oldShader = RenderSystem.getShader()
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        RenderSystem.enableDepthTest()
        RenderSystem.disableCull()
        val com1 = pattern.getCenter(1f)
        val lines1 = pattern.toLines(1f, Vec2.ZERO)
        var maxDx = -1f
        var maxDy = -1f
        for (line in lines1) {
            val dx = abs(line.x - com1.x)
            if (dx > maxDx) {
                maxDx = dx
            }
            val dy = abs(line.y - com1.y)
            if (dy > maxDy) {
                maxDy = dy
            }
        }
        val scale = 3.8f.coerceAtMost((16 / 2.5f / maxDx).coerceAtMost(16 / 2.5f / maxDy))
        val com2 = pattern.getCenter(scale)
        val lines2 = pattern.toLines(scale, com2.negated()).toMutableList()
        for (i in lines2.indices) {
            val line = lines2[i]
            lines2[i] = Vec2(line.x, -line.y)
        }
        val variance = 0.65f
        val speed = 0.1f
        val stupidHash = player.hashCode().toDouble()
        val zappy: List<Vec2> = makeZappy(lines2, findDupIndices(pattern.positions()),
                5, variance, speed, 0.2f, 0f,
                1f, stupidHash)
        val outer: Int = IXplatAbstractions.INSTANCE.getPigment(player).colorProvider.getColor(
                ClientTickCounter.getTotal() / 2f,
                patternRenderHolder.getColourPos(player.random))
        val rgbOnly = outer and 0x00FFFFFF
        var newAlpha = outer ushr 24
        if (lifetime <= 60) {
            newAlpha = floor((lifetime / 60f * 255).toDouble()).toInt()
        }
        val newARGB = newAlpha shl 24 or rgbOnly
        val inner: Int = screenCol(newARGB)
        drawLineSeq(ps.last().pose(), zappy, 0.35f, 0f, newARGB, newARGB)
        drawLineSeq(ps.last().pose(), zappy, 0.14f, 0.01f, inner, inner)
        ps.popPose()
        RenderSystem.setShader { oldShader }
        RenderSystem.enableCull()
    }
}