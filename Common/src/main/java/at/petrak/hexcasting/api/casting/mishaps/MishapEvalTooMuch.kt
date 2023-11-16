package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.world.item.DyeColor

class MishapEvalTooMuch : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLUE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.drown()
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("eval_too_deep")
}
