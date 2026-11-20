package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVecOrVecList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.msgs.MsgParticleLinesS2C
import at.petrak.hexcasting.common.msgs.MsgSingleParticleS2C
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToLong

object OpParticles : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val loc = args.getVecOrVecList(0, argc)

        // assert all locs in ambit.
        loc.map({ env.assertVecInRange(it) }, { assertVecListInRange(env, it, 32.0) })

        return SpellAction.Result(
            Spell(loc),
            loc.map({ 0.002 * MediaConstants.DUST_UNIT }, { it.size * 0.002 * MediaConstants.DUST_UNIT }).roundToLong(),
            listOf()
        )
    }

    fun assertVecListInRange(env: CastingEnvironment, list: List<Vec3>, intraRange: Double) {
        for (vec in list) {
            env.assertVecInRange(vec)
        }

        val sqrRange = intraRange * intraRange

        for (i in list.indices) {
            for (j in i until list.size) {
                if (list[i].distanceToSqr(list[j]) > sqrRange) throw MishapBadLocation(list[j])
            }
        }
    }

    data class Spell(val loc: Either<Vec3, List<Vec3>>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val colouriser = IXplatAbstractions.INSTANCE.getPigment(env.castingEntity as? ServerPlayer)

            loc.map({
                IXplatAbstractions.INSTANCE.sendPacketNear(it, 128.0, env.world, MsgSingleParticleS2C(it, colouriser))
            }, {
                if (it.isNotEmpty()) {
                    val first = it[0]
                    IXplatAbstractions.INSTANCE.sendPacketNear(first, 128.0, env.world, MsgParticleLinesS2C(it, colouriser))
                }
            })
        }
    }
}