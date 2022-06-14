package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapTooManyCloseParens : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.ORANGE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(LegacySpellDatum.make(errorCtx.pattern))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("too_many_close_parens")
}
