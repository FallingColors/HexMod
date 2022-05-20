package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect

object OpEval : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        val instrs: SpellList = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()
        val harness = CastingHarness(ctx)
        harness.stack.addAll(stack)
        harness.localIota = local

        val sideEffects = mutableListOf<OperatorSideEffect>()

        for (insn in instrs) {
            val res = harness.getUpdate(insn, ctx.world)
            sideEffects.addAll(res.sideEffects)
            if (res.sideEffects.any { it is OperatorSideEffect.DoMishap }) {
                break
            }
            harness.applyFunctionalData(res.newData)
        }

        return OperationResult(harness.stack, harness.localIota, sideEffects)
    }
}
