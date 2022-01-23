package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.CastingHarness

object OpEval : Operator {
    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        val instrs: List<SpellDatum<*>> = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()
        val harness = CastingHarness(ctx)
        harness.stack.addAll(stack)
        stack.clear()

        val spellsToCast = mutableListOf<RenderedSpell>()
        for (pat in instrs) {
            val res = harness.update(pat.tryGet(), ctx.world)
            when (res) {
                is CastingHarness.CastResult.Error -> throw res.exn
                is CastingHarness.CastResult.Cast -> spellsToCast.addAll(res.spells)
                else -> {}
            }
            if (res.shouldQuit()) break
        }
        stack.addAll(harness.stack)

        return OperationResult(500_000, spellsToCast)
    }
}