package at.petrak.hexcasting.common.casting.actions.world

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants

object OpDimensionQuery : ConstMediaAction {
    override val argc = 0
    override val mediaCost: Long = MediaConstants.DUST_UNIT / 10

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        return env.world.dimension().location().asActionResult
    }
}
