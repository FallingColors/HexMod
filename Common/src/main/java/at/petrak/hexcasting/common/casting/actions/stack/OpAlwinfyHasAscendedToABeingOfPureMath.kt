package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getPositiveLong
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import it.unimi.dsi.fastutil.longs.LongArrayList

// "lehmer code"
object OpAlwinfyHasAscendedToABeingOfPureMath : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        if (image.stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val code = image.stack.getPositiveLong(image.stack.lastIndex)
        val stack = image.stack.init()

        val strides = LongArrayList()
        for (f in FactorialIter()) {
            if (f <= code)
                strides.add(f)
            else
                break
        }

        if (strides.size > stack.size)
            throw MishapNotEnoughArgs(strides.size + 1, stack.size + 1)

        val base = stack.dropRight(strides.size)
        val editSlice = stack.takeRight(strides.size).toMutableList()
        var editTarget = editSlice
        val swap = editTarget.toMutableList()
        var radix = code
        for (divisor in strides.asReversed()) {
            val index = radix / divisor
            radix %= divisor
            editTarget[0] = swap.removeAt(index.toInt())
            // i hope this isn't O(n)
            editTarget = editTarget.subList(1, editTarget.size)
        }

        val result = base.appendedAll(Vector.from(editSlice))

        val image2 = image.withUsedOp().copy(stack = result)
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }

    private class FactorialIter : Iterator<Long> {
        var acc = 1L
        var n = 1L
        override fun hasNext(): Boolean = true

        override fun next(): Long {
            val out = this.acc
            this.acc *= this.n
            this.n++
            return out
        }
    }
}
