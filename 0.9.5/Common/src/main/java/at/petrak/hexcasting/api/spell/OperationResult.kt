package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

/**
 * What happens when an operator is through?
 */
data class OperationResult(val newContinuation: SpellContinuation, val newStack: List<SpellDatum<*>>, val newLocalIota: SpellDatum<*>, val sideEffects: List<OperatorSideEffect>)
