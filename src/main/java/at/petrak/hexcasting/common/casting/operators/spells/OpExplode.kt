package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
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
        val pos = args.getChecked<Vec3>(0)
        val strength = args.getChecked<Double>(1)
        ctx.assertVecInRange(pos)
        return Triple(
            Spell(pos, strength, this.fire),
            ((1 + Mth.clamp(strength.toFloat(), 0f, 10f) + if (this.fire) 2 else 0) * 50_000.0).toInt(),
            listOf(ParticleSpray.Burst(pos, strength, 50))
        )
    }

    private data class Spell(val pos: Vec3, val strength: Double, val fire: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.world.explode(
                ctx.caster,
                pos.x,
                pos.y,
                pos.z,
                Mth.clamp(strength.toFloat(), 0f, 10f),
                this.fire,
                Explosion.BlockInteraction.BREAK
            )
        }
    }
}
