package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.Util
import net.minecraft.world.item.DyeColor

/**
 * this is bad
 */
class MishapInvalidSpellDatumType(val perpetrator: Any) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        // Send it twice, just to make it clear
        val msg = this.errorMessage(ctx, errorCtx)
        ctx.caster.sendMessage(msg, Util.NIL_UUID)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("invalid_spell_datum_type", this.perpetrator.toString(), this.perpetrator.javaClass.typeName)
}
