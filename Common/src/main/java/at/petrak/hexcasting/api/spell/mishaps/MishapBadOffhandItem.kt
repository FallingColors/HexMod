package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

class MishapBadOffhandItem(val item: ItemStack, val hand: InteractionHand, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BROWN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        val item = ctx.caster.getItemInHand(hand).copy()
        ctx.caster.setItemInHand(hand, ItemStack.EMPTY.copy())

        val delta = ctx.caster.lookAngle.scale(0.5)
        yeetItem(item, ctx, delta)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        if (item.isEmpty)
            return error("no_item.offhand", actionName(errorCtx.action), wanted)

        return error("bad_item.offhand", actionName(errorCtx.action), wanted, item.count, item.displayName)
    }

    companion object {
        @JvmStatic
        fun of(item: ItemStack, hand: InteractionHand, stub: String, vararg args: Any): MishapBadOffhandItem {
            return MishapBadOffhandItem(item, hand, TranslatableComponent("hexcasting.mishap.bad_item.$stub", *args))
        }
    }
}
