package at.petrak.hexcasting.api.spell.casting.eval

import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.server.level.ServerLevel

/**
 * A list of patterns to be evaluated in sequence.
 * @property list the *remaining* list of patterns to be evaluated
 * @property isMetacasting only for sound effects, if this is being cast from a hermes / iris
 */
data class FrameEvaluate(val list: SpellList, val isMetacasting: Boolean) : ContinuationFrame {
    // Discard this frame and keep discarding frames.
    override fun breakDownwards(stack: List<Iota>) = false to stack

    // Step the list of patterns, evaluating a single one.
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingHarness
    ): CastingHarness.CastResult {
        // If there are patterns left...
        return if (list.nonEmpty) {
            val newCont = if (list.cdr.nonEmpty) { // yay TCO
                // ...enqueue the evaluation of the rest of the patterns...
                continuation.pushFrame(FrameEvaluate(list.cdr, this.isMetacasting))
            } else continuation
            // ...before evaluating the first one in the list.
            val update = harness.getUpdate(list.car, level, newCont)
            if (this.isMetacasting && update.sound != HexEvalSounds.MISHAP) {
                update.copy(sound = HexEvalSounds.HERMES)
            } else {
                update
            }
        } else {
            // If there are no patterns (e.g. empty Hermes), just return OK.
            CastingHarness.CastResult(continuation, null, ResolvedPatternType.EVALUATED, listOf(), HexEvalSounds.HERMES)
        }
    }

    override fun serializeToNBT() = NBTBuilder {
        "type" %= "evaluate"
        "patterns" %= list.serializeToNBT()
        "isMetacasting" %= isMetacasting
    }
}