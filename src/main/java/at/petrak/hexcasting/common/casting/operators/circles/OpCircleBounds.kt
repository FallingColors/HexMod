package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import net.minecraft.world.phys.Vec3

class OpCircleBounds(val max: Boolean) : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        if (ctx.spellCircle == null)
            throw MishapNoSpellCircle()

        val aabb = ctx.spellCircle.aabb

        return Operator.spellListOf(
            if (max)
                Vec3(aabb.minX, aabb.minY, aabb.minZ)
            else
                Vec3(aabb.maxX, aabb.maxY, aabb.maxZ)
        )
    }
}
