package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.lib.HexDamageTypes
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapBadBrainsweep(val mob: Mob, val pos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GREEN)

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        trulyHurt(mob, mob.damageSources().source(HexDamageTypes.OVERCAST, ctx.caster), 1f)
    }

    override fun particleSpray(ctx: CastingEnvironment): ParticleSpray {
        return ParticleSpray.burst(Vec3.atCenterOf(pos), 1.0)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("bad_brainsweep", blockAtPos(ctx, this.pos))
}
