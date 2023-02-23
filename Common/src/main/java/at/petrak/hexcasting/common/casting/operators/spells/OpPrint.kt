package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.OperationResult
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

// TODO should this dump the whole stack
object OpPrint : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingEnvironment
    ): OperationResult {
        if (stack.isEmpty()) {
            throw MishapNotEnoughArgs(1, 0)
        }
        val datum = stack[stack.lastIndex]
        return OperationResult(
            continuation, stack, ravenmind, listOf(
            OperatorSideEffect.AttemptSpell(Spell(datum), hasCastingSound = false, awardStat = false)
        )
        )
    }

    private data class Spell(val datum: Iota) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            ctx.caster.sendSystemMessage(datum.display())
        }
    }
}
