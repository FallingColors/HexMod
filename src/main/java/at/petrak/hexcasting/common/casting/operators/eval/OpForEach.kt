package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.CastingHarness
import at.petrak.hexcasting.common.casting.OperatorSideEffect

object OpForEach : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        val instrs: List<SpellDatum<*>> = stack.getChecked(stack.lastIndex - 1)
        val datums: List<SpellDatum<*>> = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        val out = mutableListOf<SpellDatum<*>>()
        val sideEffects = mutableListOf<OperatorSideEffect>()

        for (subdatum in datums) {
            ctx.incDepth()
            val harness = CastingHarness(ctx)
            harness.stack.addAll(stack)
            for (pat in instrs) {
                val res = harness.getUpdate(pat.tryGet(), ctx.world)
                sideEffects.addAll(res.sideEffects)
                if (res.sideEffects.any { it is OperatorSideEffect.Mishap }) {
                    break
                }
                harness.applyFunctionalData(res.newData)
            }
            out.addAll(harness.stack)
        }
        stack.add(SpellDatum.make(out))

        return OperationResult(stack, sideEffects)
    }
}