package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.iota.Iota

/**
 * A change to the data in a CastHarness after a pattern is drawn.
 */
data class FunctionalData(
    val stack: List<Iota>,
    val parenCount: Int,
    val parenthesized: List<Iota>,
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
