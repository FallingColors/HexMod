package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

// TODO should this dump the whole stack
object OpPrint : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()

        if (stack.isEmpty()) {
            throw MishapNotEnoughArgs(1, 0)
        }
        val datum = stack[stack.lastIndex]

        val image2 = image.withUsedOp().copy(stack = stack)
        return OperationResult(
            image2,
            listOf(
                OperatorSideEffect.AttemptSpell(Spell(datum), hasCastingSound = false, awardStat = false)
            ),
            continuation,
            HexEvalSounds.SPELL,
        )
    }

    private data class Spell(val datum: Iota) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            env.printMessage(datum.display())
        }
    }
}
