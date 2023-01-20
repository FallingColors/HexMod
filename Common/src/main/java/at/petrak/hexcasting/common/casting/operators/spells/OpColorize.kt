package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.item.ItemStack

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
            Spell(handStack),
            MediaConstants.DUST_UNIT,
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
