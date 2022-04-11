package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapInvalidIota
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.floor

object OpFloor : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val value = args.getChecked<Double>(0)
        return spellListOf(floor(value))
    }
}
