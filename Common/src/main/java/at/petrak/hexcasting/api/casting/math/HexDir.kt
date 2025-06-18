package at.petrak.hexcasting.api.casting.math

import at.petrak.hexcasting.api.utils.getSafe
import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

enum class HexDir {
    NORTH_EAST, EAST, SOUTH_EAST, SOUTH_WEST, WEST, NORTH_WEST;

    fun rotatedBy(a: HexAngle): HexDir =
        values()[(this.ordinal + a.ordinal).mod(values().size)]

    operator fun times(a: HexAngle) = this.rotatedBy(a)

    fun angleFrom(other: HexDir): HexAngle =
        HexAngle.values()[(this.ordinal - other.ordinal).mod(HexAngle.values().size)]

    operator fun minus(other: HexDir) = this.angleFrom(other)

    fun asDelta(): HexCoord =
        when (this) {
            NORTH_EAST -> HexCoord(1, -1)
            EAST -> HexCoord(1, 0)
            SOUTH_EAST -> HexCoord(0, 1)
            SOUTH_WEST -> HexCoord(-1, 1)
            WEST -> HexCoord(-1, 0)
            NORTH_WEST -> HexCoord(0, -1)
        }

    companion object {
        val CODEC: Codec<HexDir> = Codec.STRING.xmap(
            HexDir::fromString,
            HexDir::name
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, HexDir> = ByteBufCodecs.STRING_UTF8.map(
            HexDir::fromString,
            HexDir::name
        )

        @JvmStatic
        fun fromString(key: String): HexDir {
            return values().getSafe(key, WEST)
        }
    }
}
