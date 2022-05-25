package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.world.phys.Vec3

object OpConstructVec : ConstManaOperator {
    override val argc = 3
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val x = args.getChecked<Double>(0, argc)
        val y = args.getChecked<Double>(1, argc)
        val z = args.getChecked<Double>(2, argc)
        return Vec3(x, y, z).asSpellResult
    }
}
