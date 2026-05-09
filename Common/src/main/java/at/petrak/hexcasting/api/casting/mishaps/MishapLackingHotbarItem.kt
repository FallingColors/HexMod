package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

/**
 * Thrown when a pattern requires a certain item or type of item in the hotbar, but can't find any.
 *
 * @property wanted A text component describing the item or type of item that should have been present.
 */
class MishapLackingHotbarItem(val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.dropHeldItems()
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = error("bad_item.hotbar", wanted)

    companion object {
        /**
         * Creates a new MishapLackingHotbarItem using a string rather than a full text component for the
         * `wanted` field, by appending the string to the `hexcasting.mishap.bad_item.` translation key.
         */
        @JvmStatic
        fun of(stub: String, vararg args: Any): MishapLackingHotbarItem {
            return MishapLackingHotbarItem("hexcasting.mishap.bad_item.$stub".asTranslatedComponent(*args))
        }
    }
}
