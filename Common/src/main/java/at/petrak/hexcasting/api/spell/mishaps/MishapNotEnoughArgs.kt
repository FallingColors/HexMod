package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.GarbageIota
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.item.DyeColor

class MishapNotEnoughArgs(val expected: Int, val got: Int) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIGHT_GRAY)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        repeat(expected - got) { stack.add(GarbageIota()) }
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        if (got == 0)
            error("no_args", actionName(errorCtx.action), expected)
        else
            error("not_enough_args", actionName(errorCtx.action), expected, got)
}
