package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation

/**
 * What happens when an operator is through?
 */
data class OperationResult(
    val newImage: CastingImage,
    val sideEffects: List<OperatorSideEffect>,
    val newContinuation: SpellContinuation,
    val sound: EvalSound,
)
