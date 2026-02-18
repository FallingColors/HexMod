package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
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

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component? {
        val perpKey = HexIotaTypes.REGISTRY.getKey(perpetrator.getType())
        
        // this translation key is preferred because it includes the namespace
        var perpDesc = Component.translatableWithFallback("hexcasting.iota.${perpKey}.desc", "no desc found")
        // for addons that don't implement the .desc key, grab the non-namespaced invalid_value key instead
        if (perpDesc.getString() == "no desc found")
            perpDesc = Component.translatable("hexcasting.mishap.invalid_value.class.${perpKey?.getPath()}")
        
        return error(
            "invalid_value", expected, reverseIdx,
            perpDesc, perpetrator.display()
        )
    }

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
