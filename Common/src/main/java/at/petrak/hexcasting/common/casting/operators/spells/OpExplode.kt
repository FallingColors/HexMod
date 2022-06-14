package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
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
        val strength = args.getPositiveDoubleUnder(1, 10.0, argc)
        ctx.assertVecInRange(pos)
        val cost = ManaConstants.DUST_UNIT * (3 * strength + if (fire) 0.125 else 1.0)
        return Triple(
            Spell(pos, strength, this.fire),
            cost.toInt(),
            listOf(ParticleSpray.burst(pos, strength, 50))
        )
    }

    private data class Spell(val pos: Vec3, val strength: Double, val fire: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.world.mayInteract(ctx.caster, BlockPos(pos)))
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
