package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.misc.FrozenColorizer
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
