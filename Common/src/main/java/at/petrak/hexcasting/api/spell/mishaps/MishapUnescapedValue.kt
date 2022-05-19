package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.DatumType
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

/**
 * The value was a naked iota without being Considered or Retrospected.
 */
class MishapUnescapedValue(
    val perpetrator: SpellDatum<*>
) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GRAY)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        val idx = stack.indexOfLast { it.getType() == DatumType.LIST }
        if (idx != -1)
            stack[idx] = SpellDatum.make(Widget.GARBAGE)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error(
            "unescaped",
            perpetrator.display()
        )
}
