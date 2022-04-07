package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapInvalidIota
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.abs
import kotlin.math.roundToInt

object OpDuplicateN : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val countDouble = args.getChecked<Double>(1)

        if (abs(countDouble.toInt() - countDouble) >= 0.05f)
            throw MishapInvalidIota(
                args[1],
                0,
                TranslatableComponent("hexcasting.mishap.invalid_value.fisherman", args.size)
            )

        val count = countDouble.roundToInt()
        // there's gotta be a better way to do this
        val out = mutableListOf<SpellDatum<*>>()
        for (n in 0 until count)
            out.add(args[0])

        return out
    }
}