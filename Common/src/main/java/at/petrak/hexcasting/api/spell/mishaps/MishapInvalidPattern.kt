package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import net.minecraft.world.item.DyeColor

class MishapInvalidPattern : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.YELLOW)

    override fun resolutionType(ctx: CastingContext) = ResolvedPatternType.INVALID

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack.add(SpellDatum.make(Widget.GARBAGE))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("invalid_pattern")
}
