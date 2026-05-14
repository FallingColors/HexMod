package at.petrak.hexcasting.common.casting.actions.escaping

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpOpenParen : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val image2 = image.copy(
            parenCount = image.parenCount + 1
        )
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }

    override fun operateInParens(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota): Pair<OperationResult, ResolvedPatternType> {
        // we have escaped the parens onto the stack; we just also record our count.
        val newParens = image.parenthesized.toMutableList()
        newParens.add(CastingImage.ParenthesizedIota(thisIota, false))
        val image2 = image.copy(
            parenthesized = newParens,
            parenCount = image.parenCount + 1
        )
        val resolutionType = if (image.parenCount == 0) ResolvedPatternType.EVALUATED else ResolvedPatternType.ESCAPED
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE) to resolutionType
    }
}