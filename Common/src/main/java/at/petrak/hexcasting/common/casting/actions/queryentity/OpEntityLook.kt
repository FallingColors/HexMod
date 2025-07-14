package at.petrak.hexcasting.common.casting.actions.queryentity

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpEntityLook : ConstMediaAction {
    override val argc = 1

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val e = args.getEntity(0, argc)
        env.assertEntityInRange(e)
        return e.lookAngle.asActionResult
    }
}
