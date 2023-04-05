package at.petrak.hexcasting.common.casting.operators.selectors

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota

object OpGetCaster : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        if (ctx.caster == null)
            return null.asActionResult

        ctx.assertEntityInRange(ctx.caster)
        return ctx.caster.asActionResult
    }
}
