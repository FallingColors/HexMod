package at.petrak.hexcasting.common.casting.actions.escaping

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.ParenthesizedOperationResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNeedsParens
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpCloseAllParens : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        throw MishapNeedsParens()
    }

    override fun operateInParens(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, thisIota: Iota): ParenthesizedOperationResult {
        val newStack = image.stack.toMutableList()
        newStack.add(DoubleIota(image.parenCount.toDouble()))
        newStack.add(ListIota(image.parenthesized.toList().map { it.iota }))
        val image2 = image.copy(
            stack = newStack,
            parenCount = 0,
            parenthesized = listOf()
        )
        return ParenthesizedOperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE, ResolvedPatternType.EVALUATED)
    }
}