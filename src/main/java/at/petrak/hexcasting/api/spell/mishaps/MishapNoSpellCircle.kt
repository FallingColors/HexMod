package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNoSpellCircle : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIGHT_BLUE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        ctx.caster.inventory.dropAll()
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("no_spell_circle", actionName(errorCtx.action))
}
