package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.eval.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.world.item.DyeColor

class MishapNoAkashicRecord(val pos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.PURPLE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        ctx.caster.giveExperiencePoints(-100)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("no_akashic_record", pos.toShortString())
}
