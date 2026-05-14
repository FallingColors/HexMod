package at.petrak.hexcasting.common.casting.actions.escaping

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNeedsParens
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpUndo : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        throw MishapNeedsParens()
    }

    override fun operateInParens(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota): Pair<OperationResult, ResolvedPatternType> {
        val newParens = image.parenthesized.toMutableList()
        val last = newParens.removeLastOrNull()
        val newParenCount = image.parenCount + if (last == null || last.escaped || last.iota !is PatternIota) 0 else when (last.iota.pattern.angles) {
            HexActions.OPEN_PAREN.prototype.angles -> -1
            HexActions.CLOSE_PAREN.prototype.angles -> 1
            else -> 0
        }
        val image2 = image.copy(
            parenthesized = newParens,
            parenCount = newParenCount
        )
        // TODO: this should properly mishap if there was nothing to remove
        val resolutionType = if (last == null) ResolvedPatternType.ERRORED else ResolvedPatternType.UNDONE
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE) to resolutionType
    }
}