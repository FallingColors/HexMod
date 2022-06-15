package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.xplat.IXplatAbstractions

// TODO I don't think anyone has ever used this operation in the history of the world.
// TODO standardize "a negligible amount" of media to be 1/8 a dust
object OpGetSentinelWayfind : ConstManaAction {
    override val argc = 1
    override val manaCost = ManaConstants.DUST_UNIT / 10
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val from = args.getVec3(0, argc)

        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(ctx.caster)

        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())

        return if (!sentinel.hasSentinel)
            listOf(NullIota())
        else
            sentinel.position.subtract(from).normalize().asActionResult
    }
}
