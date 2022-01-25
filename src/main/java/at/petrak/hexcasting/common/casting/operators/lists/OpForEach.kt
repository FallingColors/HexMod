package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.CastingHarness
import at.petrak.hexcasting.hexmath.HexPattern

object OpForEach : Operator {
    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        val last = stack.lastIndex
        val maybeProgram = stack[last - 1]
        val vals = stack.getChecked<List<SpellDatum<*>>>(last)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        val program = when (maybeProgram.payload) {
            is HexPattern -> listOf(maybeProgram.payload)
            is List<*> -> maybeProgram.payload.map { (it as SpellDatum<*>).tryGet() }
            else -> throw CastException(CastException.Reason.OP_WRONG_TYPE, List::class.java, maybeProgram.payload)
        }

        val outvalues = mutableListOf<SpellDatum<*>>()
        val spellsToCast = mutableListOf<RenderedSpell>()
        for (v in vals) {
            ctx.incDepth()
            val harness = CastingHarness(ctx)
            // Put the entire current stack on there, then the next value
            harness.stack.addAll(stack)
            harness.stack.add(v)
            for (pat in program) {
                val res = harness.update(pat, ctx.world)
                when (res) {
                    is CastingHarness.CastResult.Error -> throw res.exn
                    is CastingHarness.CastResult.Cast -> spellsToCast.addAll(res.spells)
                    else -> {}
                }
                if (res.quitStatus() == CastingHarness.QuitStatus.QUIT) break
            }
            outvalues.addAll(harness.stack)
        }

        stack.addAll(outvalues)
        return OperationResult(10_000, spellsToCast)
    }
}