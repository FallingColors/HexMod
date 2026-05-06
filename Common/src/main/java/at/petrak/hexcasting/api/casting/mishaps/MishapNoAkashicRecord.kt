package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.core.BlockPos
import net.minecraft.world.item.DyeColor

class MishapNoAkashicRecord(val pos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.PURPLE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        env.mishapEnvironment.removeXp(100)
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("no_akashic_record", pos.toShortString())
}
