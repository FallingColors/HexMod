package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpRandom : ConstManaOperator {
    override val argc: Int
        get() = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return ctx.world.random.nextDouble().asSpellResult
    }
}
