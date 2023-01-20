package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import net.minecraft.world.item.DyeColor

class MishapTooManyCloseParens : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.ORANGE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        // TODO this is a kinda shitty mishap
        stack.add(PatternIota(errorCtx.pattern))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("too_many_close_parens")
}
