package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapInvalidSpellDatumType(val perpetrator: Any) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        val msg = this.errorMessage(ctx, errorCtx)
        ctx.caster.sendMessage(msg, Util.NIL_UUID)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("invalid_spell_datum_type", this.perpetrator.toString(), this.perpetrator.javaClass.typeName)
}