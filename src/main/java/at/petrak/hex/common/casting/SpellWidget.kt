package at.petrak.hex.common.casting

import at.petrak.hex.common.casting.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.operators.SimpleOperator

/**
 * Miscellaneous spell datums used as markers, etc.
 *
 * They act as operators that push themselves.
 */
enum class SpellWidget : SimpleOperator {
    NULL,
    OPEN_PAREN, CLOSE_PAREN, ESCAPE;

    override val argc: Int
        get() = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        spellListOf(this)
}