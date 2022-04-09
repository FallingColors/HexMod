package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.common.lib.HexCapabilities
import net.minecraft.world.phys.Vec3

object OpGetSentinelWayfind : ConstManaOperator {
    override val argc = 1
    override val manaCost = 1_000
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val from = args.getChecked<Vec3>(0)

        val maybeCap = ctx.caster.getCapability(HexCapabilities.SENTINEL).resolve()
        if (!maybeCap.isPresent)
            return spellListOf(Widget.NULL)

        val cap = maybeCap.get()
        if (cap.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(cap.dimension.location())

        val sentinelPos = if (!cap.hasSentinel)
            return spellListOf(Widget.NULL)
        else
            cap.position

        return spellListOf(sentinelPos.subtract(from).normalize())
    }
}
