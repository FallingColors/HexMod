package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.utils.HexDamageSources
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.DyeColor

class MishapAlreadyBrainswept(val villager: Villager) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIME)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        villager.hurt(HexDamageSources.OVERCAST, villager.health)
    }

    override fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray.Burst(villager.eyePosition, 1.0)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("already_brainswept")

}
