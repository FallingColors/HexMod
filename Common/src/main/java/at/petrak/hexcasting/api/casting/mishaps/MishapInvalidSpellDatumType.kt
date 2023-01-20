package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.eval.CastingContext
import net.minecraft.world.item.DyeColor

/**
 * this is bad
 */
class MishapInvalidSpellDatumType(val perpetrator: Any) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("invalid_spell_datum_type", this.perpetrator.toString(), this.perpetrator.javaClass.typeName)
}
