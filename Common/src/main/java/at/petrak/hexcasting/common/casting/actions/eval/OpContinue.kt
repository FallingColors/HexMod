package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.mishaps.MishapNeedsLoopContext
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpContinue : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        var newCont = continuation
        while (newCont is SpellContinuation.NotDone && !newCont.frame.breakSideways())
            newCont = newCont.next

        if (newCont !is SpellContinuation.NotDone) {
            // failed to find a loop frame
            throw MishapNeedsLoopContext()
        }

        return OperationResult(image.withUsedOp(), listOf(), newCont, HexEvalSounds.SPELL)
    }
}
