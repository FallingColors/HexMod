package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveDoubleUnderInclusive
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntitiesBy
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class OpExplode(val fire: Boolean) : SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        var pos = args.getVec3(0, argc)
        val strength = args.getPositiveDoubleUnderInclusive(1, 10.0, argc)
        env.assertVecInRange(pos)

        // Prevent the footgun of explosions exactly at an entity's eye position not doing damage
        val eps = 0.01;
        val epsv = Vec3(eps, eps, eps)
        val aabb = AABB(pos.subtract(epsv), pos.add(epsv))
        val tooCloseToEyePos = env.world.getEntities(null, aabb) {
            OpGetEntitiesBy.isReasonablySelectable(env, it)
        }.any { it.eyePosition.distanceToSqr(pos) == 0.0 }
        if (tooCloseToEyePos) {
            pos = pos.add(0.0, 0.000001, 0.0)
        }

        val clampedStrength = Mth.clamp(strength, 0.0, 10.0)
        val cost = MediaConstants.DUST_UNIT * (3 * clampedStrength + if (fire) 1.0 else 0.125)
        return SpellAction.Result(
            Spell(pos, strength, this.fire),
            cost.toLong(),
            listOf(ParticleSpray.burst(pos, strength, 50))
        )
    }

    private data class Spell(val pos: Vec3, val strength: Double, val fire: Boolean) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // TODO: you can use this to explode things *outside* of the worldborder?
            if (!env.canEditBlockAt(BlockPos.containing(pos)))
                return

            env.world.explode(
                env.castingEntity,
                pos.x,
                pos.y,
                pos.z,
                strength.toFloat(),
                this.fire,
                Level.ExplosionInteraction.TNT
            )
        }
    }
}
