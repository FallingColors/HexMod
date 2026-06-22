package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpContinue : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        var newCont = continuation
        while (newCont is SpellContinuation.NotDone && !newCont.frame.breakSideways())
            newCont = newCont.next

        var newStack = image.stack.toList()
        // we didn't hit any loops, so we have fully exited the hex
        if (newCont !is SpellContinuation.NotDone) {
            // clear the stack so staffcasting exits
            newStack = listOf()
        }

        val image2 = image.withUsedOp().copy(stack = newStack)
        return OperationResult(image2, listOf(), newCont, HexEvalSounds.SPELL)
    }
}
