package at.petrak.hexcasting.api.casting.math

import com.mojang.serialization.Codec

enum class HexAngle {
    FORWARD, RIGHT, RIGHT_BACK, BACK, LEFT_BACK, LEFT;

    fun rotatedBy(a: HexAngle) = values()[(this.ordinal + a.ordinal) % values().size]
    operator fun times(a: HexAngle) = this.rotatedBy(a)

    companion object {
        val CODEC: Codec<HexAngle> =
            Codec.BYTE.xmap({ ordinal -> HexAngle.values()[ordinal.toInt()] }, { dir -> dir.ordinal.toByte() })
    }
}
