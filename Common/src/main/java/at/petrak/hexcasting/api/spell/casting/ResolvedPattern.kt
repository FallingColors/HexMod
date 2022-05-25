package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.nbt.CompoundTag
import java.util.*


data class ResolvedPattern(val pattern: HexPattern, val origin: HexCoord, var type: ResolvedPatternType) {
    fun serializeToNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.put("Pattern", pattern.serializeToNBT())
        tag.putInt("OriginQ", origin.q)
        tag.putInt("OriginR", origin.r)
        tag.putString("Valid", type.name.lowercase(Locale.ROOT))
        return tag
    }

    companion object {
        @JvmStatic
        fun fromNBT(tag: CompoundTag): ResolvedPattern {
            val pattern = HexPattern.fromNBT(tag.getCompound("Pattern"))
            val origin = HexCoord(tag.getInt("OriginQ"), tag.getInt("OriginR"))
            val valid = try {
                ResolvedPatternType.valueOf(tag.getString("Valid").uppercase(Locale.ROOT))
            } catch (e: IllegalArgumentException) {
                ResolvedPatternType.UNRESOLVED
            }
            return ResolvedPattern(pattern, origin, valid)
        }
    }
}
