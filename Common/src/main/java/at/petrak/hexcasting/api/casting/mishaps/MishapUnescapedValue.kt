package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.world.item.DyeColor

/**
 * The value was a naked iota without being Considered or Retrospected.
 */
class MishapUnescapedValue(
    val perpetrator: Iota
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GRAY)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // TODO
        /*
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
         */
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("unescaped", perpetrator.display())
}
