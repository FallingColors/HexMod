package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexPattern

/**
 * A change to the data in a CastHarness after a pattern is drawn.
 *
 * [wasThisPatternInvalid] is for the benefit of the controller.
 */
data class FunctionalData(
    val stack: List<SpellDatum<*>>,
    val parenCount: Int,
    val parenthesized: List<HexPattern>,
    val escapeNext: Boolean,
) {
    /**
     * Whether this by itself is enough to get the client to quit casting.
     *
     * Note the client may want to quit for other reasons.
     */
    fun shouldQuit(): Boolean =
        this.stack.isEmpty() && this.parenCount == 0 && !this.escapeNext
}
