package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.item.ItemStack

object OpColorize : SpellOperator {
    override val argc = 0

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn(IXplatAbstractions.INSTANCE::isColorizer)
        if (!IXplatAbstractions.INSTANCE.isColorizer(handStack)) {
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "colorizer"
            )
        }
        return Triple(
            Spell(handStack),
            ManaConstants.DUST_UNIT,
            listOf()
        )
    }

    private data class Spell(val stack: ItemStack) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val copy = stack.copy()
            if (ctx.withdrawItem(copy, 1, true)) {
                IXplatAbstractions.INSTANCE.setColorizer(
                    ctx.caster,
                    FrozenColorizer(copy, ctx.caster.uuid)
                )
            }
        }
    }
}
