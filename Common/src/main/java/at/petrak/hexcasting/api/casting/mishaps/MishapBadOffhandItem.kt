package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

class MishapBadOffhandItem(val item: ItemStack, val hand: InteractionHand, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BROWN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        yeetHeldItem(ctx, hand)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) = if (item.isEmpty)
        error("no_item.offhand", wanted)
    else
        error("bad_item.offhand", wanted, item.count, item.displayName)

    companion object {
        @JvmStatic
        fun of(item: ItemStack, hand: InteractionHand, stub: String, vararg args: Any): MishapBadOffhandItem {
            return MishapBadOffhandItem(item, hand, "hexcasting.mishap.bad_item.$stub".asTranslatedComponent(*args))
        }
    }
}
