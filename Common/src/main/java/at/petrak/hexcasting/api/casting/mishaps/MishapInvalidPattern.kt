package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapInvalidPattern(val pattern: HexPattern?) : Mishap() {
    @Deprecated("Provide the pattern that caused the mishap as an argument")
    constructor() : this(null) {}

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.YELLOW)

    override fun resolutionType(ctx: CastingEnvironment) = ResolvedPatternType.INVALID

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(GarbageIota())
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component? {
        if (pattern == null) return error("invalid_pattern_generic")
        return error("invalid_pattern", PatternIota.display(pattern))
    }
}
