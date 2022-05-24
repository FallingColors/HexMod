package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import net.minecraft.Util

object OpPrint : Operator {
    override fun operate(continuation: SpellContinuation, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty()) {
            throw MishapNotEnoughArgs(1, 0)
        }
        val datum = stack[stack.lastIndex]
        return OperationResult(
            continuation, stack, local, listOf(
                OperatorSideEffect.AttemptSpell(Spell(datum), hasCastingSound = false, awardStat = false)
            )
        )
    }

    private data class Spell(val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.caster.sendMessage(
                datum.display(),
                Util.NIL_UUID
            )
        }
    }
}
