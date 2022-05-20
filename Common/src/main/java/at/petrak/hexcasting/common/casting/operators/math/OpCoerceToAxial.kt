package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

object OpCoerceToAxial : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val vec = args.getChecked<Vec3>(0, argc)
        if (vec == Vec3.ZERO)
            return vec.asSpellResult
        return Vec3.atLowerCornerOf(Direction.getNearest(vec.x, vec.y, vec.z).normal).asSpellResult
    }
}
