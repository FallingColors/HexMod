package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

class OpExplode(val fire: Boolean) : SpellOperator {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getChecked<Vec3>(0, argc)
        val strength = args.getChecked<Double>(1, argc)
        ctx.assertVecInRange(pos)
        val clampedStrength = Mth.clamp(strength, 0.0, 10.0)
        val cost = ManaConstants.DUST_UNIT * (3 * clampedStrength + if (fire) 1.0 else 0.125)
        return Triple(
            Spell(pos, clampedStrength, this.fire),
            cost.toInt(),
            listOf(ParticleSpray.burst(pos, clampedStrength, 50))
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
