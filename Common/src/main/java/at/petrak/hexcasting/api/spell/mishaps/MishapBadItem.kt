package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor

class MishapBadItem(val item: ItemEntity, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BROWN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        item.deltaMovement = item.deltaMovement.add((Math.random() - 0.5) * 0.05, 0.75, (Math.random() - 0.5) * 0.05)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) = if (item.item.isEmpty)
        error("no_item", actionName(errorCtx.action), wanted)
    else
        error("bad_item", actionName(errorCtx.action), wanted, item.item.count, item.item.displayName)

    companion object {
        @JvmStatic
        fun of(item: ItemEntity, stub: String): MishapBadItem {
            return MishapBadItem(item, "hexcasting.mishap.bad_item.$stub".asTranslatedComponent)
        }
    }
}
