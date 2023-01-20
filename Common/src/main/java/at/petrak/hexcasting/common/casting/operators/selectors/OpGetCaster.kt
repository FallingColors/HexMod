package at.petrak.hexcasting.common.casting.operators.selectors

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota

object OpGetCaster : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        ctx.assertEntityInRange(ctx.caster)
        return ctx.caster.asActionResult
    }
}
