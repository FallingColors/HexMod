package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.abs
import kotlin.math.roundToInt

object OpLastNToList : Operator {
    override fun operate(continuation: MutableList<ContinuationFrame>, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val arg = stack.takeLast(1).getChecked<Double>(0)
        val datum = stack[stack.lastIndex]
        stack.removeLast()
        if (arg < 0 || arg > stack.size || abs(arg.roundToInt() - arg) >= 0.05f) {
            throw MishapInvalidIota(
                datum,
                0,
                TranslatableComponent("hexcasting.mishap.invalid_value.int.between", 0, stack.size)
            )
        }
        val output = mutableListOf<SpellDatum<*>>()
        output.addAll(stack.takeLast(arg.toInt()))
        val endSize = stack.size - output.toList().size
        while (stack.size != endSize) {
            stack.removeLast()
        }
        stack.addAll(output.asSpellResult)

        return OperationResult(stack, local, listOf())
    }
}
