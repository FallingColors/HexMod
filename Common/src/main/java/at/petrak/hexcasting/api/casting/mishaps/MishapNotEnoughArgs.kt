package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.world.item.DyeColor

class MishapNotEnoughArgs(val expected: Int, val got: Int) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.LIGHT_GRAY)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        repeat(expected - got) { stack.add(GarbageIota()) }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        if (got == 0)
            error("no_args", expected)
        else
            error("not_enough_args", expected, got)
}
