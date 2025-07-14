package at.petrak.hexcasting.common.casting.actions.circles

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.utils.Vector

object OpImpetusPos : ConstMediaAction {
    override val argc = 0

    override fun execute(args: Vector<Iota>, ctx: CastingEnvironment): Vector<Iota> {
        if (ctx !is CircleCastEnv)
            throw MishapNoSpellCircle()

        return ctx.circleState().impetusPos.asActionResult
    }
}
