package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum

/**
 * Miscellaneous spell datums used as markers, etc.
 *
 * They act as operators that push themselves.
 */
enum class Widget : ConstManaOperator {
    NULL,
    OPEN_PAREN, CLOSE_PAREN, ESCAPE;

    override val argc: Int
        get() = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        spellListOf(this)
}