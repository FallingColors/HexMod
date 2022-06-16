package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.getPositiveInt
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import it.unimi.dsi.fastutil.ints.IntArrayList

// "lehmer code"
object OpAlwinfyHasAscendedToABeingOfPureMath : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val code = stack.getPositiveInt(stack.lastIndex)

        val strides = IntArrayList()
        for (f in FactorialIter()) {
            if (f <= code)
                strides.add(f)
            else
                break
        }

        if (strides.size > stack.size)
            throw MishapNotEnoughArgs(strides.size + 1, stack.size + 1)
        var editTarget = stack.subList(stack.size - strides.size, stack.size)
        val swap = editTarget.toMutableList()
        var radix = code
        for (divisor in strides.asReversed()) {
            val index = radix / divisor
            radix %= divisor
            editTarget[0] = swap.removeAt(index)
            // i hope this isn't O(n)
            editTarget = editTarget.subList(1, editTarget.size)
        }

        // val cost = (ln((strides.lastOrNull() ?: 0).toFloat()) * ManaConstants.DUST_UNIT).toInt()

        return OperationResult(
            continuation,
            stack,
            ravenmind,
            listOf()
        )
    }

    private class FactorialIter : Iterator<Int> {
        var acc = 1
        var n = 1
        override fun hasNext(): Boolean = true

        override fun next(): Int {
            val out = this.acc
            this.acc *= this.n
            this.n++
            return out
        }
    }
}
