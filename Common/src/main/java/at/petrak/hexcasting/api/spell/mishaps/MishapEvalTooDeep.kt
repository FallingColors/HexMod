package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.item.DyeColor

class MishapEvalTooDeep : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLUE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        ctx.caster.airSupply -= 290
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("eval_too_deep")
}
