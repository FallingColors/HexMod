package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import net.minecraft.world.phys.Vec3

object OpGetSentinelWayfind : ConstManaOperator {
    override val argc = 1
    override val manaCost = 1_000
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val from = args.getChecked<Vec3>(0)

        val sentinel = HexPlayerDataHelper.getSentinel(ctx.caster)

        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())

        val sentinelPos = if (!sentinel.hasSentinel)
            return spellListOf(Widget.NULL)
        else
            sentinel.position

        return spellListOf(sentinelPos.subtract(from).normalize())
    }
}
