package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.getPositiveInt
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

// Yes this is weird in that 1=remove, 0=keep, but i think the UX is better
// todo this is untested
object OpBitMask : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (stack.size < 1)
            throw MishapNotEnoughArgs(1, 0)

        val mask = stack.getPositiveInt(stack.lastIndex)
        stack.removeLast()

        val reqdArgc = Int.SIZE_BITS - mask.countLeadingZeroBits()
        if (stack.size < reqdArgc) {
            // remember, we already popped one
            throw MishapNotEnoughArgs(reqdArgc + 1, stack.size + 1)
        }

        val out = mutableListOf<Iota>()
        for (i in 0 until reqdArgc) {
            // we have you surrounded do bitwise operations in kotlin
            val bit = mask and (1 shl i) == 1
            if (!bit) {
                out.add(stack[stack.lastIndex - i])
            }
        }

        return OperationResult(continuation, out.asReversed(), ravenmind, listOf())
    }
}
