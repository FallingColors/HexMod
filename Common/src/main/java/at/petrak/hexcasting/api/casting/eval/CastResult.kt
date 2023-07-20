package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota

/**
 * The result of doing something to a cast harness.
 *
 * Contains the iota that was executed to produce this CastResult,
 * the next thing to execute after this is finished, the modified state of the stack,
 * and side effects, as well as display information for the client.
 */
data class CastResult(
        val cast: Iota,
        val continuation: SpellContinuation,
        val newData: CastingImage?,
        val sideEffects: List<OperatorSideEffect>,
        val resolutionType: ResolvedPatternType,
        val sound: EvalSound,
)
