package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getLong
import at.petrak.hexcasting.api.spell.iota.Iota

object OpNot : ConstManaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val num = args.getLong(0, argc)
        return num.inv().asActionResult
    }
}
