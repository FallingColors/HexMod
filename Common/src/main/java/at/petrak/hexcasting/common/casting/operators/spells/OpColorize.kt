package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpColorize : SpellAction {
    override val argc = 0

    override fun execute(
        args: List<Iota>,
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
            Spell,
            ManaConstants.DUST_UNIT,
            listOf()
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val handStack = ctx.getHeldItemToOperateOn(IXplatAbstractions.INSTANCE::isColorizer).first.copy()
            if (IXplatAbstractions.INSTANCE.isColorizer(handStack)) {
                if (ctx.withdrawItem(handStack.item, 1, true)) {
                    IXplatAbstractions.INSTANCE.setColorizer(
                        ctx.caster,
                        FrozenColorizer(handStack, ctx.caster.uuid)
                    )
                }
            }
        }
    }
}
