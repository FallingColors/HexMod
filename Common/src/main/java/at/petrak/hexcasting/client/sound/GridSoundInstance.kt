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

class GridSoundInstance(val player: Player) :
    AbstractTickableSoundInstance(HexSounds.CASTING_AMBIANCE, SoundSource.PLAYERS) {
    var mousePosX: Double = 0.5
    var mousePosY: Double = 0.5

    init {
        val lookVec = player.lookAngle
        val playerPos = player.eyePosition
        this.x = playerPos.x + lookVec.x
        this.y = playerPos.y + lookVec.y
        this.z = playerPos.z + lookVec.z
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
            val newPos = player.eyePosition
                .add(normalVector)
                .add(horizontalPlanarVector.scale(PAN_SCALE * 2 * (mousePosX - 0.5)))
                .add(verticalPlanarVector.scale(PAN_SCALE * 2 * (mousePosY - 0.5)))
            this.x = newPos.x
            this.y = newPos.y
            this.z = newPos.z
        }
    }

    private fun calculateVectorFromPitchAndYaw(pitch: Float, yaw: Float): Vec3 {
        val radiansPitch = pitch * Mth.DEG_TO_RAD
        val radiansYaw = -yaw * Mth.DEG_TO_RAD
        val xComponent = Mth.cos(radiansYaw).toDouble()
        val zComponent = Mth.sin(radiansYaw).toDouble()
        val azimuthHorizontal = Mth.cos(radiansPitch).toDouble()
        val azimuthVertical = Mth.sin(radiansPitch).toDouble()
        return Vec3(zComponent * azimuthHorizontal, -azimuthVertical, xComponent * azimuthHorizontal)
    }

    companion object {
        const val PAN_SCALE = 0.5
    }
}
