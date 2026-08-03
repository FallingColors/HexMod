package at.petrak.hexcasting.common.casting.actions.environment

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants

object OpGetMedia : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val mediaRaw = Long.MAX_VALUE - env.extractMedia(Long.MAX_VALUE, true)

        return (mediaRaw.toDouble() / MediaConstants.DUST_UNIT.toDouble()).asActionResult
    }
}
