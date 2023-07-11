package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.world.item.DyeColor

class MishapTooManyCloseParens : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.ORANGE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // TODO this is a kinda shitty mishap
        if (errorCtx.pattern != null)
            stack.add(PatternIota(errorCtx.pattern))
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("too_many_close_parens")
}
