package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpToSet : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val payload = args.getChecked<SpellList>(0)
        // augh
        val out = mutableListOf<SpellDatum<*>>()
        // i am not sure of a better way to do this
        for (v in payload) {
            if (out.none { it.equalsWithDoubleTolerance(v) }) {
                out.add(v)
            }
        }
        return out
    }
}
