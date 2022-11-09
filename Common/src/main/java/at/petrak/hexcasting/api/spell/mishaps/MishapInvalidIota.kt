package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.GarbageIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

/**
 * The value failed some kind of predicate.
 *
 * [MishapInvalidIota.reverseIdx] is the index from the *back* of the stack.
 */
class MishapInvalidIota(
    val perpetrator: Iota,
    val reverseIdx: Int,
    val expected: Component
) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GRAY)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        stack[stack.size - 1 - reverseIdx] = GarbageIota();
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error(
            "invalid_value", actionName(errorCtx.action), expected, reverseIdx,
            perpetrator.display()
        )

    companion object {
        @JvmStatic
        fun ofType(perpetrator: Iota, reverseIdx: Int, name: String): MishapInvalidIota {
            return of(perpetrator, reverseIdx, "class.$name")
        }

        @JvmStatic
        fun of(perpetrator: Iota, reverseIdx: Int, name: String, vararg translations: Any): MishapInvalidIota {
            val key = "hexcasting.mishap.invalid_value.$name"
            return MishapInvalidIota(perpetrator, reverseIdx, key.asTranslatedComponent(*translations))
        }
    }
}
