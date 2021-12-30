package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.SpellOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.CastingHarness
import at.petrak.hex.common.casting.SpellDatum

object OpEval : SpellOperator {
    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): Int {
        val instrs: List<SpellDatum<*>> = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()
        val ctxDeeper = ctx.withIncDepth()
        val harness = CastingHarness.Default(ctxDeeper)
        harness.stack.addAll(stack)
        stack.clear()
        for (pat in instrs) {
            val res = harness.update(pat.tryGet())
            if (res is CastingHarness.CastResult.Error) {
                throw res.exn
            }
            // in ANY OTHER CASE JUST KEEP GOING
            // including if there's RenderedSpells on the stack or the stack becomes clear
        }
        stack.addAll(harness.stack)

        return 50
    }
}