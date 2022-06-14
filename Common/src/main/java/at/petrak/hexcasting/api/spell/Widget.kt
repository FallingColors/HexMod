package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext
import java.util.*

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

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> =
        this.asSpellResult

    companion object {
        @JvmStatic
        fun fromString(key: String): Widget {
            val lowercaseKey = key.lowercase(Locale.ROOT)
            return values().firstOrNull { it.name.lowercase(Locale.ROOT) == lowercaseKey } ?: GARBAGE
        }
    }
}
