package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

/**
 * The value failed some kind of predicate.
 */
class MishapInvalidIota(
    val perpetrator: Iota,
    val reverseIdx: Int,
    val expected: Component
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GRAY)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack[stack.size - 1 - reverseIdx] = GarbageIota();
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error(
            "invalid_value", expected, reverseIdx,
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
