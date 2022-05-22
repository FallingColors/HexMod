package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3

object OpGetSentinelWayfind : ConstManaOperator {
    override val argc = 1
    override val manaCost = ManaConstants.DUST_UNIT / 10
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val from = args.getChecked<Vec3>(0, argc)

        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(ctx.caster)

        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())

        return if (!sentinel.hasSentinel)
            null.asSpellResult
        else
            sentinel.position.subtract(from).normalize().asSpellResult
    }
}
