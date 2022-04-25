package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.asin

object OpArcSin : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val value = args.getChecked<Double>(0)
        if (value < -1 || value > 1)
            throw MishapInvalidIota(
                SpellDatum.make(value),
                0,
                TranslatableComponent("hexcasting.mishap.invalid_value.double.between", -1, 1)
            )
        return spellListOf(asin(value))
    }
}
