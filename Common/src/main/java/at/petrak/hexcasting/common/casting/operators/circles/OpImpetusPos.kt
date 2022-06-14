package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import net.minecraft.world.phys.Vec3

object OpImpetusPos : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        if (ctx.spellCircle == null)
            throw MishapNoSpellCircle()

        return Vec3.atCenterOf(ctx.spellCircle.impetusPos).asSpellResult
    }
}
