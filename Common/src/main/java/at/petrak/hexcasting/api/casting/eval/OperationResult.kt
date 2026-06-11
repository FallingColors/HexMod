package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation

interface IOperationResult {
    val newImage: CastingImage
    val sideEffects: List<OperatorSideEffect>
    val newContinuation: SpellContinuation
    val sound: EvalSound
}

/**
 * What happens when an operator is through?
 */
data class OperationResult(
    override val newImage: CastingImage,
    override val sideEffects: List<OperatorSideEffect>,
    override val newContinuation: SpellContinuation,
    override val sound: EvalSound,
) : IOperationResult

/**
 * What happens when an operator is through while parenthesized?
 */
data class ParenthesizedOperationResult(
    override val newImage: CastingImage,
    override val sideEffects: List<OperatorSideEffect>,
    override val newContinuation: SpellContinuation,
    override val sound: EvalSound,
    val resolutionType: ResolvedPatternType,
) : IOperationResult
