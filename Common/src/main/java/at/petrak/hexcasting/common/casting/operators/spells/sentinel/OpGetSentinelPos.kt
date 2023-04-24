package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpGetSentinelPos : ConstMediaAction {
    override val argc = 0
    override val mediaCost = MediaConstants.DUST_UNIT / 10
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(env.caster)
        if (sentinel.dimension != env.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        return if (sentinel.hasSentinel)
            sentinel.position.asActionResult
        else
            listOf(NullIota())
    }
}
