package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNoSpellCircle : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.PURPLE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        ctx.caster.drop(true)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("no_spell_circle", actionName(errorCtx.action!!))
}