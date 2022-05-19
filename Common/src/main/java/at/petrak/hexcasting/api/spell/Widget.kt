package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.casting.CastingContext

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
        spellListOf(this)
}
