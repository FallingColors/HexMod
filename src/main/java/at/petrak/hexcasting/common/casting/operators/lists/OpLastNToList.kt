package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.abs
import kotlin.math.roundToInt

object OpLastNToList : Operator {
    val manaCost: Int
        get() = 0

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
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
        stack.addAll(spellListOf(output))

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMana(this.manaCost))

        return OperationResult(stack, sideEffects)
    }
}
