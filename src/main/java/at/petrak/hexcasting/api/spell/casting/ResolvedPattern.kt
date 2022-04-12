package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.nbt.CompoundTag


data class ResolvedPattern(val pattern: HexPattern, val origin: HexCoord, var valid: ResolvedPatternValidity) {
    fun serializeToNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.put("Pattern", pattern.serializeToNBT())
        tag.putInt("OriginQ", origin.q)
        tag.putInt("OriginR", origin.r)
        tag.putInt("Valid", valid.ordinal)
        return tag
    }

    companion object {
        @JvmStatic
        fun DeserializeFromNBT(tag: CompoundTag): ResolvedPattern {
            val pattern = HexPattern.DeserializeFromNBT(tag.getCompound("Pattern"))
            val origin = HexCoord(tag.getInt("OriginQ"), tag.getInt("OriginR"))
            val valid = ResolvedPatternValidity.values()[tag.getInt("Valid").coerceIn(0, ResolvedPatternValidity.values().size - 1)]
            return ResolvedPattern(pattern, origin, valid)
        }
    }
}
