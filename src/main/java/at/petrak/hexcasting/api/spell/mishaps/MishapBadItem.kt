package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

class MishapBadItem(val item: ItemStack, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BROWN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        // not really sure what to do here?
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        if (item.isEmpty)
            error("no_item", actionName(errorCtx.action), wanted)
        return error("bad_item", actionName(errorCtx.action), wanted, item)
    }

    companion object {
        @JvmStatic
        fun of(item: ItemStack, stub: String): MishapBadItem {
            return MishapBadItem(item, TranslatableComponent("hexcasting.mishap.bad_item.$stub"))
        }
    }
}
