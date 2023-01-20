package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.item.DyeColor

class MishapInvalidPattern : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.YELLOW)

    override fun resolutionType(ctx: CastingContext) = ResolvedPatternType.INVALID

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(GarbageIota())
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("invalid_pattern")
}
