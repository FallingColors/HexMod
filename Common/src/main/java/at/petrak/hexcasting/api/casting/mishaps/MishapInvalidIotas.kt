package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.DyeColor

/**
 * The value failed some kind of predicate.
 */
class MishapInvalidOperatorArgs(
    val perpetrators: List<Iota>,
    val operator: HexPattern
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GRAY)

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        for (i in perpetrators.indices) {
            stack[stack.size - 1 - i] = GarbageIota()
        }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error(
            "invalid_operator_args", operator, perpetrators.fold(MutableComponent.create(ComponentContents.EMPTY)) { mc, iota -> mc.append(iota.display()) }
        )
}
