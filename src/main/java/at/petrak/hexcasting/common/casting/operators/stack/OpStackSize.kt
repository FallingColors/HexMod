package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpStackSize : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        stack.add(SpellDatum.make(stack.size.toDouble()))
        return OperationResult(stack, local, listOf())
    }
}
