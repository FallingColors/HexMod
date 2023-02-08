package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.item.DyeColor

class MishapShameOnYou() : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val caster = ctx.caster
        if (caster != null) {
            // FIXME: handle null caster case
            Mishap.trulyHurt(caster, HexDamageSources.SHAME, 69420f)
        }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = error("shame")
}
