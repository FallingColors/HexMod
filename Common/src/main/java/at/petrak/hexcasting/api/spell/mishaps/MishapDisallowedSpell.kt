package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapDisallowedSpell : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun resolutionType(ctx: CastingContext) = ResolvedPatternType.INVALID

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("disallowed", actionName(errorCtx.action))
}
