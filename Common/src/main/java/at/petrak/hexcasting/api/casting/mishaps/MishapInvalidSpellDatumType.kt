package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.Vector
import net.minecraft.world.item.DyeColor

/**
 * this is bad
 */
class MishapInvalidSpellDatumType(val perpetrator: Any) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun executeReturnStack(env: CastingEnvironment, errorCtx: Context, stack: Vector<Iota>): Vector<Iota> =
        stack

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("invalid_spell_datum_type", this.perpetrator.toString(), this.perpetrator.javaClass.typeName)
}
