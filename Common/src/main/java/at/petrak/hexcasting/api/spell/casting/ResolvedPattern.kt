package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.NBTBuilder
import net.minecraft.nbt.CompoundTag
import java.util.*


data class ResolvedPattern(val pattern: HexPattern, val origin: HexCoord, var type: ResolvedPatternType) {
    fun serializeToNBT() = NBTBuilder {
        "Pattern" %= pattern.serializeToNBT()
        "OriginQ" %= origin.q
        "OriginR" %= origin.r
        "Valid" %= type.name.lowercase(Locale.ROOT)
    }

    companion object {
        @JvmStatic
        fun fromNBT(tag: CompoundTag): ResolvedPattern {
            val pattern = HexPattern.fromNBT(tag.getCompound("Pattern"))
            val origin = HexCoord(tag.getInt("OriginQ"), tag.getInt("OriginR"))
            val valid = ResolvedPatternType.fromString(tag.getString("Valid"))
            return ResolvedPattern(pattern, origin, valid)
        }
    }
}
