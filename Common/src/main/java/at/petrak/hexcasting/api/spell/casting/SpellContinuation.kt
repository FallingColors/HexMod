package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness.CastResult
import net.minecraft.server.level.ServerLevel

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {
    object Done: SpellContinuation {}

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation): SpellContinuation {}

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)
}
