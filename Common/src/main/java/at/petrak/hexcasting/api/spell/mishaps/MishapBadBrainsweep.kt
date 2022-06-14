package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapBadBrainsweep(val villager: Villager, val pos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GREEN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        trulyHurt(villager, HexDamageSources.overcastDamageFrom(ctx.caster), villager.health)
    }

    override fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray.burst(Vec3.atCenterOf(pos), 1.0)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("bad_brainsweep", blockAtPos(ctx, this.pos))
}
