package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.iota.Iota

/**
 * What happens when an operator is through?
 */
data class OperationResult(
    val newContinuation: SpellContinuation,
    val newStack: List<Iota>,
    val newRavenmind: Iota?,
    val sideEffects: List<OperatorSideEffect>
)
