package at.petrak.hexcasting.common.casting.actions.spells.sentinel

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer

object OpGetSentinelPos : ConstMediaAction {
    override val argc = 0
    override val mediaCost: Long = MediaConstants.DUST_UNIT / 10
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env.castingEntity !is ServerPlayer)
            throw MishapBadCaster()

        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(env.castingEntity as? ServerPlayer) ?: return listOf(NullIota.INSTANCE)
        if (sentinel.dimension != env.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        return sentinel.position.asActionResult
    }
}
