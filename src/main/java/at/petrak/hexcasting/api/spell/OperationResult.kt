package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.common.casting.OperatorSideEffect

/**
 * What happens when an operator is through?
 */
data class OperationResult(val newStack: List<SpellDatum<*>>, val sideEffects: List<OperatorSideEffect>)
