package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.sounds.SoundEvent

/**
 * Sound that plays as a side-effect-adjacent when casting
 */
enum class EvalSound {
    /** Silence */
    NONE,

    /** The generic "bvwonh" sound for non-spell ops */
    GENERIC,

    /** The "bwoink!" for spell ops */
    SPELL_BOINK,

    /** The "gvwoh" for mishaps */
    MISHAP;

    /**
     * Which sound type has the greater priority?
     */
    fun greaterOf(that: EvalSound): EvalSound =
        EvalSound.values()[maxOf(this.ordinal, that.ordinal)]

    fun soundEvent(): SoundEvent? = when (this) {
        NONE -> null
        GENERIC -> HexSounds.ADD_PATTERN
        SPELL_BOINK -> HexSounds.ACTUALLY_CAST
        MISHAP -> HexSounds.FAIL_PATTERN
    }
}