package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellOperator.Companion.assertVecInRange
import at.petrak.hex.common.casting.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.operators.SimpleOperator
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

object OpExplode : SimpleOperator, RenderedSpellImpl {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val pos = args.getChecked<Vec3>(0)
        assertVecInRange(pos, ctx)
        return spellListOf(RenderedSpell(OpExplode, spellListOf(pos)))
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val pos = args.getChecked<Vec3>(0)

        // 4.0 is the strength of TNT, i guess
        ctx.world.explode(ctx.caster, pos.x, pos.y, pos.z, 4.0f, Explosion.BlockInteraction.BREAK)
    }
}