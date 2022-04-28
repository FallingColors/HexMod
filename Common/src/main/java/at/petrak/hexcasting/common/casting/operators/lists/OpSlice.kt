package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.util.Mth
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object OpSlice : ConstManaOperator {
    override val argc = 3
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0)
        val index1 = Mth.clamp(args.getChecked<Double>(1).roundToInt(), 0, list.size)
        val index2 = Mth.clamp(args.getChecked<Double>(2).roundToInt(), 0, list.size)

        if (index1 == index2)
            return spellListOf(listOf<SpellDatum<*>>())

        return spellListOf(list.subList(min(index1, index2), max(index1, index2)))
    }
}
