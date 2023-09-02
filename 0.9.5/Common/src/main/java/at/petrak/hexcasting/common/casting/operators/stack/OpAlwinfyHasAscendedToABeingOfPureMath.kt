package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import it.unimi.dsi.fastutil.ints.IntArrayList
import kotlin.math.abs
import kotlin.math.roundToInt

// "lehmer code"
object OpAlwinfyHasAscendedToABeingOfPureMath : Operator {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<SpellDatum<*>>,
        local: SpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val codeDouble = stack.getChecked<Double>(stack.lastIndex)
        if (abs(codeDouble.roundToInt() - codeDouble) >= 0.05f)
            throw MishapInvalidIota(
                stack.last(),
                0,
                "hexcasting.mishap.invalid_value.int".asTranslatedComponent(0)
            )
        stack.removeLast()
        val code = codeDouble.roundToInt()

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

//        val cost = (ln((strides.lastOrNull() ?: 0).toFloat()) * ManaConstants.DUST_UNIT).toInt()

        return OperationResult(
            continuation,
            stack,
            local,
            listOf() // OperatorSideEffect.ConsumeMana(cost)
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
