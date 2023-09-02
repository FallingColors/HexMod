package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota

object OpRandom : ConstMediaAction {
    override val argc: Int
        get() = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return ctx.world.random.nextDouble().asActionResult
    }
}
