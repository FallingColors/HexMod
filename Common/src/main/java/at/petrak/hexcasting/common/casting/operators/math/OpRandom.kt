package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota

object OpRandom : ConstMediaAction {
    override val argc: Int
        get() = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        return ctx.world.random.nextDouble().asActionResult
    }
}
