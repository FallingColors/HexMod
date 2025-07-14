package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.common.lib.HexDamageTypes
import net.minecraft.world.item.DyeColor

class MishapStackSize() : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun resolutionType(ctx: CastingEnvironment) = ResolvedPatternType.ERRORED

    override fun executeReturnStack(env: CastingEnvironment, errorCtx: Context, stack: Vector<Iota>): Vector<Iota> =
        Vector.from(listOf(GarbageIota()))

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("stack_size")
}
