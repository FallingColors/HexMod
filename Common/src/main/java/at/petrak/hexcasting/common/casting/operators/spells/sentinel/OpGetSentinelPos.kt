package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpGetSentinelPos : ConstManaAction {
    override val argc = 0
    override val manaCost = ManaConstants.DUST_UNIT / 10
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(ctx.caster)
        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        return if (sentinel.hasSentinel)
            sentinel.position.asActionResult
        else
            listOf(NullIota())
    }
}
