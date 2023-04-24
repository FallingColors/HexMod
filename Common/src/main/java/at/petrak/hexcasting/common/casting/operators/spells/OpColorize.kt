package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.item.ItemStack

object OpColorize : SpellAction {
    override val argc = 0

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn(IXplatAbstractions.INSTANCE::isColorizer)
                ?: throw MishapBadOffhandItem.of(ItemStack.EMPTY, null, "colorizer") // TODO: hack

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
        override fun cast(ctx: CastingEnvironment) {
            val copy = stack.copy()
            val caster = ctx.caster
            if (caster != null && ctx.withdrawItem(copy::equals, 1, true)) {
                IXplatAbstractions.INSTANCE.setColorizer(
                    ctx.caster,
                    FrozenColorizer(copy, caster.uuid)
                )
            }
        }
    }
}
