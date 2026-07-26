package at.petrak.hexcasting.common.casting.actions.escaping

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ParenthesizedOperationResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNeedsParens
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpCloseParen : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        throw MishapNeedsParens()
    }

    override fun operateInParens(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota): ParenthesizedOperationResult {
        val newParenCount = image.parenCount - 1
        if (newParenCount == 0) {
            val newStack = image.stack.appended(ListIota(image.parenthesized.map { it.iota }))
            val image2 = image.copy(
                stack = newStack,
                parenCount = newParenCount,
                parenthesized = TreeList.empty()
            )
            return ParenthesizedOperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE, ResolvedPatternType.EVALUATED)
        } else {
            // we have this situation: "(()"
            // we need to add the close paren
            val newParens = image.parenthesized.appended(ParenthesizedIota(thisIota, false))
            val image2 = image.copy(
                parenCount = newParenCount,
                parenthesized = newParens
            )
            return ParenthesizedOperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE, ResolvedPatternType.ESCAPED)
        }
    }
}