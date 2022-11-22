package at.petrak.hexcasting.api.spell.casting.eval

import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.server.level.ServerLevel

/**
 * A stack marker representing the end of a Hermes evaluation,
 * so that we know when to stop removing frames during a Halt.
 */
object FrameFinishEval : ContinuationFrame {
    // Don't do anything else to the stack, just finish the halt statement.
    override fun breakDownwards(stack: List<Iota>) = true to stack

    // Evaluating it does nothing; it's only a boundary condition.
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingHarness
    ): CastingHarness.CastResult {
        return CastingHarness.CastResult(
            continuation,
            FunctionalData(harness.stack.toList(), 0, listOf(), false, harness.ravenmind),
            ResolvedPatternType.EVALUATED,
            listOf(),
            HexEvalSounds.NOTHING,
        )
    }

    override fun serializeToNBT() = NBTBuilder {
        "type" %= "end"
    }
}