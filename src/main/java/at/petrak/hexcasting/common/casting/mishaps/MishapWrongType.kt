package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.DyeColor

class MishapWrongType(val perpetrator: SpellDatum<*>, val reverseIdx: Int, val expectedKey: String) : Mishap() {
    override fun accentColor(ctx: CastingContext): FrozenColorizer =
        dyeColor(DyeColor.GRAY)

    override fun execute(ctx: CastingContext, stack: MutableList<SpellDatum<*>>) {
        stack[stack.size - 1 - reverseIdx] = SpellDatum.make(Widget.GARBAGE)
    }

    override fun errorMessage(ctx: CastingContext): Component =
        error("invalid_value", TranslatableComponent(expectedKey), perpetrator.display())
}