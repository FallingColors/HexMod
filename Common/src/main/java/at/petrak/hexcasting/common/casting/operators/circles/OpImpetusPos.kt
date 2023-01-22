package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNoSpellCircle

object OpImpetusPos : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        if (ctx.spellCircle == null)
            throw MishapNoSpellCircle()

        return ctx.spellCircle.impetusPos.asActionResult
    }
}
