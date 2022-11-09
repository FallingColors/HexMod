package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.iota.Iota

/**
 * What happens when an operator is through?
 */
data class OperationResult(
    val newContinuation: SpellContinuation,
    val newStack: List<Iota>,
    val newRavenmind: Iota?,
    val sideEffects: List<OperatorSideEffect>
)
