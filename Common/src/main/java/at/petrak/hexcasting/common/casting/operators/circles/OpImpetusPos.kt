package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle

object OpImpetusPos : ConstManaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        if (ctx.spellCircle == null)
            throw MishapNoSpellCircle()

        return ctx.spellCircle.impetusPos.asActionResult
    }
}
