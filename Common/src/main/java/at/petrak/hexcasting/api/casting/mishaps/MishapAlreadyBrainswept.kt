package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.common.lib.HexDamageTypes
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.DyeColor

class MishapAlreadyBrainswept(val mob: Mob) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GREEN)

    override fun executeReturnStack(ctx: CastingEnvironment, errorCtx: Context, stack: Vector<Iota>): Vector<Iota> {
        mob.hurt(mob.damageSources().source(HexDamageTypes.OVERCAST, ctx.castingEntity), mob.health)
        return stack
    }

    override fun particleSpray(ctx: CastingEnvironment) =
        ParticleSpray.burst(mob.eyePosition, 1.0)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("already_brainswept")

}
