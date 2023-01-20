package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.DyeColor

class MishapAlreadyBrainswept(val mob: Mob) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GREEN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        mob.hurt(HexDamageSources.overcastDamageFrom(ctx.caster), mob.health)
    }

    override fun particleSpray(ctx: CastingContext) =
        ParticleSpray.burst(mob.eyePosition, 1.0)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("already_brainswept")

}
