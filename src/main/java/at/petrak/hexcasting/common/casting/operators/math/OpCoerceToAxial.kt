package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

object OpCoerceToAxial : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val vec = args.getChecked<Vec3>(0)
        if (vec == Vec3.ZERO)
            return spellListOf(vec)
        return spellListOf(Vec3.atLowerCornerOf(Direction.getNearest(vec.x, vec.y, vec.z).normal))
    }
}
