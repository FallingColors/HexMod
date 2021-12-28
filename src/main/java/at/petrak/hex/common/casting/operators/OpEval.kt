package at.petrak.hex.common.casting.operators

import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.CastingHarness
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellOperator.Companion.getChecked

object OpEval : SimpleOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val instrs: List<SpellDatum<*>> = args.getChecked(0)

        val harness = CastingHarness.Default(ctx)
        for (pat in instrs) {
            val res = harness.update(pat.tryGet())
            if (res is CastingHarness.CastResult.Error) {
                throw res.exn
            }
            // in ANY OTHER CASE JUST KEEP GOING
            // including if there's RenderedSpells on the stack or the stack becomes clear
        }
        return harness.stack
    }
}