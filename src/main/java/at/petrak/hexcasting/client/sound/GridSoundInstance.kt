package at.petrak.hexcasting.client.sound

import at.petrak.hexcasting.client.gui.GuiSpellcasting
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class GridSoundInstance(val player: Player) : AbstractTickableSoundInstance(HexSounds.CASTING_AMBIANCE.get(), SoundSource.PLAYERS) {
    var mousePosX: Double = 0.5
    var mousePosY: Double = 0.5

    init {
        val lookVec = player.lookAngle
        this.x = player.x + lookVec.x
        this.y = player.y + lookVec.y
        this.z = player.z + lookVec.z
        this.attenuation = SoundInstance.Attenuation.LINEAR
        this.looping = true
        this.delay = 0
        this.relative = false
    }

    override fun tick() {
        val minecraft = Minecraft.getInstance()
        val screen = minecraft.screen
        if (screen !is GuiSpellcasting)
            stop()
        else {
            val horizontalPlanarVector = calculateVectorFromPitchAndYaw(player.xRot + 90, player.yRot)
            val verticalPlanarVector = calculateVectorFromPitchAndYaw(player.xRot, player.yRot + 90)
            val normalVector = calculateVectorFromPitchAndYaw(player.xRot, player.yRot)
            val newPos = player.position()
                .add(normalVector)
                .add(horizontalPlanarVector.scale(4 * (mousePosX - 0.5)))
                .add(verticalPlanarVector.scale(4 * (mousePosY - 0.5)))
            this.x = newPos.x
            this.y = newPos.y
            this.z = newPos.z
        }
    }

    private fun calculateVectorFromPitchAndYaw(pitch: Float, yaw: Float): Vec3 {
        val radiansPitch = pitch * (Math.PI.toFloat() / 180f)
        val radiansYaw = -yaw * (Math.PI.toFloat() / 180f)
        val xComponent = Mth.cos(radiansYaw).toDouble()
        val zComponent = Mth.sin(radiansYaw).toDouble()
        val azimuthHorizontal = Mth.cos(radiansPitch).toDouble()
        val azimuthVertical = Mth.sin(radiansPitch).toDouble()
        return Vec3(zComponent * azimuthHorizontal, -azimuthVertical, xComponent * azimuthHorizontal)
    }
}
