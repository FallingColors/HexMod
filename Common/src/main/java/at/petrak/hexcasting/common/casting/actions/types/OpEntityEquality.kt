package at.petrak.hexcasting.common.casting.actions.types

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota

object OpEntityEquality : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val entityA = args.getEntity(env.world, 0).also { env.assertEntityInRange(it) }
        val entityB = args.getEntity(env.world, 1).also { env.assertEntityInRange(it) }

        return (entityA.type == entityB.type).asActionResult
    }
}
