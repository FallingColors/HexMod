package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveDoubleUnderInclusive
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

class OpExplode(val fire: Boolean) : SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val pos = args.getVec3(0, argc)
        val strength = args.getPositiveDoubleUnderInclusive(1, 10.0, argc)
        ctx.assertVecInRange(pos)

        val clampedStrength = Mth.clamp(strength, 0.0, 10.0)
        val cost = MediaConstants.DUST_UNIT * (3 * clampedStrength + if (fire) 1.0 else 0.125)
        return SpellAction.Result(
            Spell(pos, strength, this.fire),
            cost.toInt(),
            listOf(ParticleSpray.burst(pos, strength, 50))
        )
    }

    private data class Spell(val pos: Vec3, val strength: Double, val fire: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            // TODO: you can use this to explode things *outside* of the worldborder?
            if (!ctx.canEditBlockAt(BlockPos(pos)))
                return

            ctx.world.explode(
                ctx.caster,
                pos.x,
                pos.y,
                pos.z,
                strength.toFloat(),
                this.fire,
                Explosion.BlockInteraction.BREAK
            )
        }
    }
}
