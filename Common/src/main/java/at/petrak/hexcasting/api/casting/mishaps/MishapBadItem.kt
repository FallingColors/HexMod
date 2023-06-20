package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor

class MishapBadItem(val item: ItemEntity, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        item.deltaMovement = item.deltaMovement.add((Math.random() - 0.5) * 0.05, 0.75, (Math.random() - 0.5) * 0.05)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = if (item.item.isEmpty)
        error("no_item", wanted)
    else
        error("bad_item", wanted, item.item.count, item.item.displayName)

    companion object {
        @JvmStatic
        fun of(item: ItemEntity, stub: String): MishapBadItem {
            return MishapBadItem(item, "hexcasting.mishap.bad_item.$stub".asTranslatedComponent)
        }
    }
}
