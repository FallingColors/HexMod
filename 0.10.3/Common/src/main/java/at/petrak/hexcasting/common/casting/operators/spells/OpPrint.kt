package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.casting.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

// TODO should this dump the whole stack
object OpPrint : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
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
        override fun cast(ctx: CastingContext) {
            ctx.caster.sendSystemMessage(datum.display())
        }
    }
}
