package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.lib.HexDamageTypes
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.DyeColor

class MishapAlreadyBrainswept(val mob: Mob) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GREEN)

<<<<<<< HEAD
    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        mob.hurt(HexDamageSources.overcastDamageFrom(env.caster), mob.health)
=======
    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        mob.hurt(mob.damageSources().source(HexDamageTypes.OVERCAST, ctx.caster), mob.health)
>>>>>>> talia-1.20/1.20.1
    }

    override fun particleSpray(ctx: CastingEnvironment) =
        ParticleSpray.burst(mob.eyePosition, 1.0)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("already_brainswept")

}
