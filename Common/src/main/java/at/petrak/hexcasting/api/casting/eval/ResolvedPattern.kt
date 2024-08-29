package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.math.HexCoord
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.deserializeWithCodec
import at.petrak.hexcasting.api.utils.serializeWithCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import java.util.*


data class ResolvedPattern(val pattern: HexPattern, val origin: HexCoord, var type: ResolvedPatternType) {
    constructor(pattern: HexPattern, originQ: Int, originR: Int, type: ResolvedPatternType) : this(
        pattern,
        HexCoord(originQ, originR),
        type
    )

    val originQ: Int
        get() = origin.q

    val originR: Int
        get() = origin.r

    @Deprecated("Use the CODEC instead.")
    fun serializeToNBT() = this.serializeWithCodec(CODEC)

    companion object {
        @JvmField
        val CODEC: Codec<ResolvedPattern> = RecordCodecBuilder.create {
            it.group(
                HexPattern.CODEC.fieldOf("Pattern").forGetter(ResolvedPattern::pattern),
                Codec.INT.fieldOf("OriginQ").forGetter(ResolvedPattern::originQ),
                Codec.INT.fieldOf("OriginR").forGetter(ResolvedPattern::originR),
                Codec.STRING.fieldOf("Valid")
                    .xmap(ResolvedPatternType::fromString) { type -> type.name.lowercase(Locale.ROOT) }
                    .forGetter(ResolvedPattern::type)
            ).apply(it, ::ResolvedPattern)
        }

        @Deprecated("Use the CODEC instead.")
        @JvmStatic
        fun fromNBT(tag: CompoundTag): ResolvedPattern = tag.deserializeWithCodec(CODEC)!!
    }
}
