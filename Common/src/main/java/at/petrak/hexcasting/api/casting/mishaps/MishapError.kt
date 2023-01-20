package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.world.item.DyeColor

class MishapError(val exception: Exception) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("unknown", exception)
}
