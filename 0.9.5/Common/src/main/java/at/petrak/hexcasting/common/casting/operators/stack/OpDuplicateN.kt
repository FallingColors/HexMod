package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import kotlin.math.abs
import kotlin.math.roundToInt

object OpDuplicateN : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val countDouble = args.getChecked<Double>(1, argc)

        if (abs(countDouble.roundToInt() - countDouble) >= 0.05f)
            throw MishapInvalidIota(
                args[1],
                0,
                "hexcasting.mishap.invalid_value.int.between".asTranslatedComponent(0, args.size)
            )

        val count = countDouble.roundToInt()
        // there's gotta be a better way to do this
        val out = mutableListOf<SpellDatum<*>>()
        for (n in 0 until count)
            out.add(args[0])

        return out
    }
}
