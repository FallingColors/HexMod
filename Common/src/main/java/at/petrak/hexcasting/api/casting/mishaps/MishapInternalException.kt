package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.world.item.DyeColor

class MishapInternalException(val exception: Exception) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("unknown", exception)
}
