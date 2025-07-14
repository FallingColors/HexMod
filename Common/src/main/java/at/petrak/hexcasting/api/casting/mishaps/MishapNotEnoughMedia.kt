package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.world.item.DyeColor

class MishapNotEnoughMedia(private val cost: Long) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.RED)

    override fun resolutionType(ctx: CastingEnvironment) = ResolvedPatternType.ERRORED

    override fun executeReturnStack(env: CastingEnvironment, errorCtx: Context, stack: Vector<Iota>): Vector<Iota> {
        env.extractMedia(cost, false)
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = "hexcasting.message.cant_overcast".asTranslatedComponent
}