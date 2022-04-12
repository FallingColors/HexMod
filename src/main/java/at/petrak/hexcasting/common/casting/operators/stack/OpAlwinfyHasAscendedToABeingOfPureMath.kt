package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

// "lehmer code"
object OpAlwinfyHasAscendedToABeingOfPureMath : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0) // todo: better message?

        val codeDouble = stack.getChecked<Double>(stack.lastIndex)
        if (abs(codeDouble.roundToInt() - codeDouble) >= 0.05f)
            throw MishapInvalidIota(
                stack.last(),
                0,
                TranslatableComponent("hexcasting.mishap.invalid_value.int", 0)
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

        val cost = (ln((strides.lastOrNull() ?: 0).toFloat()) * 10000).toInt()

        return OperationResult(
            stack,
            listOf(OperatorSideEffect.ConsumeMana(cost))
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
