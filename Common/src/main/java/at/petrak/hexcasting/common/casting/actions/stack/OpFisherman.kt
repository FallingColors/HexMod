package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import kotlin.math.abs
import kotlin.math.roundToInt

object OpFisherman : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        if (image.stack.size < 2)
            throw MishapNotEnoughArgs(2, image.stack.size)

        val x = image.stack.last()
        var stack = image.stack.init()
        val maxIdx = stack.size - 1
        val depth = let {
            if (x is DoubleIota) {
                val double = x.double
                val rounded = double.roundToInt()
                if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in -maxIdx..maxIdx) {
                    return@let rounded
                }
            }
            throw MishapInvalidIota.of(x, 0, "int.between", -maxIdx, maxIdx)
        }

        if (depth >= 0) {
            val fish = stack[stack.size - 1 - depth]
            stack = stack.dropRight(depth + 1).appendedAll(stack.takeRight(depth)).appended(fish)
        } else {
            val lure = stack.last()
            stack = stack.dropRight(1 - depth).appended(lure).appendedAll(stack.takeRight(1 - depth).init())
        }

        val image2 = image.withUsedOp().copy(stack = stack)
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}
