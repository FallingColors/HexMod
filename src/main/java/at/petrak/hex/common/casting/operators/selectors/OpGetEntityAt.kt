package at.petrak.hex.common.casting.operators.selectors

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.Widget
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object OpGetEntityAt : ConstManaOperator {
    override val argc = 1
    override val manaCost = 1000
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val pos = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(pos)
        val aabb = AABB(pos.add(Vec3(-0.5, -0.5, -0.5)), pos.add(Vec3(0.5, 0.5, 0.5)))
        val entitiesGot = ctx.world.getEntities(null, aabb)
        val entity = entitiesGot.getOrNull(0) ?: Widget.NULL
        return spellListOf(entity)
    }
}