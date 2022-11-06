package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.utils.getSafe

/**
 * Miscellaneous spell datums used as markers, etc.
 *
 * They act as operators that push themselves.
 */
enum class Widget : ConstManaOperator {
    NULL,
    OPEN_PAREN, CLOSE_PAREN, ESCAPE,
    GARBAGE;

    override val argc: Int
        get() = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        this.asSpellResult

    companion object {
        @JvmStatic
        fun fromString(key: String): Widget {
            return values().getSafe(key, GARBAGE)
        }
    }
}
