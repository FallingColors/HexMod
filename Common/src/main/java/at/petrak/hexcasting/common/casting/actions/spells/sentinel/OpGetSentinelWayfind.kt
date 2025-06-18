package at.petrak.hexcasting.common.casting.actions.spells.sentinel

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer

// TODO I don't think anyone has ever used this operation in the history of the world.
// TODO standardize "a negligible amount" of media to be 1/8 a dust
object OpGetSentinelWayfind : ConstMediaAction {
    override val argc = 1
    override val mediaCost: Long = MediaConstants.DUST_UNIT / 10
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env.castingEntity !is ServerPlayer)
            throw MishapBadCaster()

        val from = args.getVec3(0, argc)

        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(env.castingEntity as? ServerPlayer) ?: return listOf(NullIota.INSTANCE)

        if (sentinel.dimension != env.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())

        return sentinel.position.subtract(from).normalize().asActionResult
    }
}
