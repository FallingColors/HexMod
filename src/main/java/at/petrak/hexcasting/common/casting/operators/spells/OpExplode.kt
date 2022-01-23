package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.util.Mth
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

class OpExplode(val fire: Boolean) : SpellOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val pos = args.getChecked<Vec3>(0)
        val strength = args.getChecked<Double>(1)
        ctx.assertVecInRange(pos)
        return Pair(
            Spell(pos, strength, this.fire),
            ((1 + strength + if (this.fire) 2 else 0) * 50_000.0).toInt(),
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