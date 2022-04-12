package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.player.HexPlayerDataHelper

object OpColorize : SpellOperator {
    override val argc = 0

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn { FrozenColorizer.isColorizer(it) }
        if (!FrozenColorizer.isColorizer(handStack)) {
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "colorizer"
            )
        }
        return Triple(
            Spell,
            10_000,
            listOf()
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn { FrozenColorizer.isColorizer(it) }
            if (FrozenColorizer.isColorizer(handStack)) {
                if (ctx.withdrawItem(handStack.item, 1, true)) {
                    HexPlayerDataHelper.setColorizer(ctx.caster,
                        FrozenColorizer(handStack, ctx.caster.uuid)
                    )
                }
            }
        }
    }
}
