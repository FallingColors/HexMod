package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.item.DyeColor

class MishapShameOnYou() : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        Mishap.trulyHurt(ctx.caster, HexDamageSources.SHAME, 69420f)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) = error("shame")
}
