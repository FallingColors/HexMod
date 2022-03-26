package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapTooManyCloseParens(val paren: HexPattern) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.PINK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack.add(SpellDatum.make(paren))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("too_many_close_parens")
}