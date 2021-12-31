package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.util.Mth
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

object OpExplode : SpellOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val pos = args.getChecked<Vec3>(0)
        val strength = args.getChecked<Double>(1)
        ctx.assertVecInRange(pos)
        return Pair(
            Spell(pos, strength),
            (strength * 100.0).toInt(),
        )
    }

    private data class Spell(val pos: Vec3, val strength: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.world.explode(
                ctx.caster,
                pos.x,
                pos.y,
                pos.z,
                Mth.clamp(strength.toFloat(), 0f, 10f),
                Explosion.BlockInteraction.BREAK
            )
        }
    }
}