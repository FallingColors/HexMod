package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpForEach : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs: SpellList = stack.getChecked(stack.lastIndex - 1)
        val datums: SpellList = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        val out = mutableListOf<SpellDatum<*>>()
        val sideEffects = mutableListOf<OperatorSideEffect>()

        var localIota = local

        for (subdatum in datums) {
            ctx.incDepth()
            val harness = CastingHarness(ctx)
            harness.stack.addAll(stack)
            harness.stack.add(subdatum)
            harness.localIota = localIota
            for (insn in instrs) {
                val res = harness.getUpdate(insn, ctx.world)
                sideEffects.addAll(res.sideEffects)
                if (res.sideEffects.any { it is OperatorSideEffect.DoMishap }) {
                    return OperationResult(harness.stack, harness.localIota, sideEffects)
                }
                harness.applyFunctionalData(res.newData)
            }
            out.addAll(harness.stack)
            localIota = harness.localIota
        }
        stack.add(SpellDatum.make(out))

        return OperationResult(stack, localIota, sideEffects)
    }
}
