package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.math.HexCoord
import at.petrak.hexcasting.api.casting.math.HexPattern
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec


data class ResolvedPattern(val pattern: HexPattern, val origin: HexCoord, var type: ResolvedPatternType) {

    constructor(pattern: HexPattern, q: Int, r: Int, type: ResolvedPatternType): this(pattern, HexCoord(q, r), type)

    companion object {
        @JvmField
        val CODEC: Codec<ResolvedPattern> = RecordCodecBuilder.create({instance -> instance.group(
            HexPattern.CODEC.fieldOf("pattern").forGetter { it.pattern },
            Codec.INT.fieldOf("OriginQ").forGetter { it.origin.q },
            Codec.INT.fieldOf("OriginR").forGetter { it.origin.r },
            Codec.STRING.xmap(
                { ResolvedPatternType.fromString(it) },
                { it.name.lowercase() }
            ).fieldOf("Valid").forGetter { it.type }
        ).apply(instance, ::ResolvedPattern)
        })

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResolvedPattern> = StreamCodec.composite(
            HexPattern.STREAM_CODEC, ResolvedPattern::pattern,
            ByteBufCodecs.VAR_INT, { it.origin.q },
            ByteBufCodecs.VAR_INT, { it.origin.r },
            ByteBufCodecs.STRING_UTF8.map(
                { ResolvedPatternType.fromString(it) },
                { it.name.lowercase() }
            ), ResolvedPattern::type,
            ::ResolvedPattern
        )
    }
}
