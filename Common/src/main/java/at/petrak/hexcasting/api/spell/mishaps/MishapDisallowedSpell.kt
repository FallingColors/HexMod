package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import net.minecraft.world.item.DyeColor

class MishapDisallowedSpell(val type: String = "disallowed") : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun resolutionType(ctx: CastingContext) = ResolvedPatternType.INVALID

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error(type, actionName(errorCtx.action))
}
