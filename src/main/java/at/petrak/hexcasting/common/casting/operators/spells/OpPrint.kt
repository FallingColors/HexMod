package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect
import net.minecraft.Util

object OpPrint : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty()) {
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, 1, stack.size)
        }
        val datum = stack[stack.lastIndex]
        return OperationResult(
            stack, listOf(
                OperatorSideEffect.AttemptSpell(Spell(datum), false)
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