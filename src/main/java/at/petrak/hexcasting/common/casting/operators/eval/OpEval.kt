package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.network.chat.TranslatableComponent

object OpEval : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        val instrs: List<SpellDatum<*>> = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()
        val harness = CastingHarness(ctx)
        harness.stack.addAll(stack)

        val sideEffects = mutableListOf<OperatorSideEffect>()

        for (pat in instrs) {
            val pattern = if (pat.payload is HexPattern) {
                pat.payload
            } else {
                throw MishapInvalidIota(pat, 0, TranslatableComponent("hexcasting.mishap.invalid_value.list.pattern"))
            }
            val res = harness.getUpdate(pattern, ctx.world)
            sideEffects.addAll(res.sideEffects)
            if (res.sideEffects.any { it is OperatorSideEffect.DoMishap }) {
                break
            }
            harness.applyFunctionalData(res.newData)
        }
        stack.addAll(harness.stack)

        return OperationResult(harness.stack, sideEffects)
    }
}
