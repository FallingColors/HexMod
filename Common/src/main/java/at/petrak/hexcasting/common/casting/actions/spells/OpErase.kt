package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.item.ItemStack

object OpErase : SpellAction {
    override val argc = 0

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            (hexHolder?.hasHex() == true) ||
                (datumHolder?.writeIota(null, true) == true)
        } ?: throw MishapBadOffhandItem.of(ItemStack.EMPTY.copy(), null, "eraseable") // TODO: hack

        val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)

        if ((hexHolder?.hasHex() != true) &&
            (datumHolder?.writeIota(null, true) != true)
        ) {
            throw MishapBadOffhandItem.of(handStack, hand, "eraseable")
        }

        return SpellAction.Result(
            Spell(handStack),
            MediaConstants.DUST_UNIT, listOf()
        )
    }

    private data class Spell(val stack: ItemStack) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(stack)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack)

            if (hexHolder?.hasHex() == true)
                hexHolder.clearHex()

            if (datumHolder != null && datumHolder.writeIota(null, true))
                datumHolder.writeIota(null, false)
        }
    }
}
