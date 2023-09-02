package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.DatumType
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
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
        if (idx != -1) {
            val list = stack[idx].payload as SpellList
            val idxOfIota = list.indexOfFirst { it == perpetrator }
            if (idxOfIota != -1) {
                stack[idx] = SpellDatum.make(list.modifyAt(idxOfIota) {
                    SpellList.LPair(SpellDatum.make(Widget.GARBAGE), it.cdr)
                })
            }
        }
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("unescaped", perpetrator.display())
}
