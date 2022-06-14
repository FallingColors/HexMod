package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.DyeColor

class MishapAlreadyBrainswept(val villager: Villager) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GREEN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        villager.hurt(HexDamageSources.overcastDamageFrom(ctx.caster), villager.health)
    }

    override fun particleSpray(ctx: CastingContext) =
        ParticleSpray.burst(villager.eyePosition, 1.0)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("already_brainswept")

}
