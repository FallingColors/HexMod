package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.CastingHarness
import at.petrak.hexcasting.common.casting.OperatorSideEffect

object OpEval : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        val instrs: List<SpellDatum<*>> = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()
        val harness = CastingHarness(ctx)
        harness.stack.addAll(stack)

        val sideEffects = mutableListOf<OperatorSideEffect>()

        for (pat in instrs) {
            val res = harness.getUpdate(pat.tryGet(), ctx.world)
            sideEffects.addAll(res.sideEffects)
            if (res.sideEffects.any { it is OperatorSideEffect.Mishap }) {
                break
            }
            harness.applyFunctionalData(res.newData)
        }
        stack.addAll(harness.stack)

        return OperationResult(harness.stack, sideEffects)
    }
}