package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

class OpExplode(val fire: Boolean) : SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getVec3(0, argc)
        val strength = args.getPositiveDoubleUnderInclusive(1, 10.0, argc)
        ctx.assertVecInRange(pos)
        val clampedStrength = Mth.clamp(strength, 0.0, 10.0)
        val cost = MediaConstants.DUST_UNIT * (3 * clampedStrength + if (fire) 1.0 else 0.125)
        return Triple(
            Spell(pos, strength, this.fire),
            cost.toInt(),
            listOf(ParticleSpray.burst(pos, strength, 50))
        )
    }

    private data class Spell(val pos: Vec3, val strength: Double, val fire: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
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
