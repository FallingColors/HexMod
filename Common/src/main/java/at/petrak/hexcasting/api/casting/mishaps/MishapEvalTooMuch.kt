package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.world.item.DyeColor

class MishapEvalTooMuch : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLUE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        env.mishapEnvironment.drown()
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("eval_too_much")
}
