package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor

class MishapBadItem(val item: ItemEntity, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BROWN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        item.deltaMovement = item.deltaMovement.add((Math.random() - 0.5) * 0.05, 1.0, (Math.random() - 0.5) * 0.05)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        return if (item.item.isEmpty)
            error("no_item", actionName(errorCtx.action), wanted)
        else
            error("bad_item", actionName(errorCtx.action), wanted, item.item)
    }

    companion object {
        @JvmStatic
        fun of(item: ItemEntity, stub: String): MishapBadItem {
            return MishapBadItem(item, TranslatableComponent("hexcasting.mishap.bad_item.$stub"))
        }
    }
}
