package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.LegacySpellDatum

/**
 * A change to the data in a CastHarness after a pattern is drawn.
 */
data class FunctionalData(
    val stack: List<LegacySpellDatum<*>>,
    val parenCount: Int,
    val parenthesized: List<LegacySpellDatum<*>>,
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
