package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.FunctionalData
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation

/**
 * The result of doing something to a cast harness.
 *
 * Contains the next thing to execute after this is finished, the modified state of the stack,
 * and side effects, as well as display information for the client.
 */
data class CastResult(
    val continuation: SpellContinuation,
    val newData: FunctionalData?,
    val sideEffects: List<OperatorSideEffect>,
    val resolutionType: ResolvedPatternType,
    val sound: EvalSound,
)