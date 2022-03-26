package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapEvalTooDeep : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLUE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        ctx.caster.airSupply -= 290
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("eval_too_deep")
}