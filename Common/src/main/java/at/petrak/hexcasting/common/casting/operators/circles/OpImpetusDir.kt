package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNoSpellCircle

object OpImpetusDir : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        if (ctx !is CircleCastEnv)
            throw MishapNoSpellCircle()

        return ctx.startDir.step().asActionResult
    }
}
