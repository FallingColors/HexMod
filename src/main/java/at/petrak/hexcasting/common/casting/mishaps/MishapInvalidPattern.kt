package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapInvalidPattern : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.YELLOW)


    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack.add(SpellDatum.make(Widget.GARBAGE))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("invalid_pattern")
}