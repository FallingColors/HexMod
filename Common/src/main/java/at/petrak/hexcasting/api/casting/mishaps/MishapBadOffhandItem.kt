package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

class MishapBadOffhandItem(val item: ItemStack?, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.dropHeldItems()
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = if (item?.isEmpty == false)
        error("bad_item.offhand", wanted, item.count, item.displayName)
    else
        error("no_item.offhand", wanted)

    companion object {
        @JvmStatic
        fun of(item: ItemStack?, stub: String, vararg args: Any): MishapBadOffhandItem {
            return MishapBadOffhandItem(item, "hexcasting.mishap.bad_item.$stub".asTranslatedComponent(*args))
        }
    }
}
