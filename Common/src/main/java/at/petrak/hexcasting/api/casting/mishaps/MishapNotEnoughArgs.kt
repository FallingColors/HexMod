package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.world.item.DyeColor

class MishapNotEnoughArgs(val expected: Int, val got: Int) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIGHT_GRAY)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        repeat(expected - got) { stack.add(GarbageIota()) }
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        if (got == 0)
            error("no_args", expected)
        else
            error("not_enough_args", expected, got)
}
