package at.petrak.hexcasting.common.casting.actions.circles

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle

// TODO: we now have the interesting potential to add *other* spell circle getters, like the current position
// of the eval. Hmm hm hm.
// Reminded of "targeted position" in Psi -- we could have a "cast location" refl that gets the player pos
// or the current eval pos on a circle
object OpImpetusDir : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        if (ctx !is CircleCastEnv)
            throw MishapNoSpellCircle()

        return ctx.circleState().impetusDir.step().asActionResult
    }
}
