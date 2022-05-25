package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.spellListOf
import net.minecraft.world.phys.Vec3

object OpDeconstructVec : ConstManaOperator {
    override val argc = 1
    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val v = args.getChecked<Vec3>(0, argc)
        return spellListOf(v.x, v.y, v.z)
    }
}
