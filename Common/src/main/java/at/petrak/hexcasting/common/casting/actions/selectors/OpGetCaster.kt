package at.petrak.hexcasting.common.casting.actions.selectors

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpGetCaster : ConstMediaAction {
    override val argc = 0

    override fun execute(args: Vector<Iota>, ctx: CastingEnvironment): Vector<Iota> {
        if (ctx.castingEntity == null)
            return null.asActionResult

        ctx.assertEntityInRange(ctx.castingEntity)
        return ctx.castingEntity.asActionResult
    }
}
