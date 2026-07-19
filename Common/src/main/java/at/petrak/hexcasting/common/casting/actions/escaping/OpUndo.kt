package at.petrak.hexcasting.common.casting.actions.escaping

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ParenthesizedOperationResult
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

    override fun operateInParens(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota): ParenthesizedOperationResult {
        val (newParens, last) = if(image.parenthesized.isEmpty()) {
            (image.parenthesized to null)
        } else {
            (image.parenthesized.init() to image.parenthesized.last())
        }
        var newParenCount = image.parenCount
        if (last == null) {
            // if there was nothing in the parenthesized list, undo the initial open paren
            newParenCount--
        } else if (last.iota is PatternIota && !last.escaped) {
            // adjust paren count if undoing a non-escaped open or close paren
            when (last.iota.pattern.angles) {
                HexActions.OPEN_PAREN.prototype.angles -> newParenCount--
                HexActions.CLOSE_PAREN.prototype.angles -> newParenCount++
            }
        }
        val image2 = image.copy(
            parenthesized = newParens,
            parenCount = newParenCount
        )
        return ParenthesizedOperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE, ResolvedPatternType.UNDONE)
    }
}