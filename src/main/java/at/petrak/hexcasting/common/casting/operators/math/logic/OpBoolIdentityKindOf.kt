package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import net.minecraft.world.phys.Vec3

object OpBoolIdentityKindOf : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return spellListOf(
            when (args[0].payload) {
                Widget.NULL -> 0.0
                0.0, Vec3.ZERO -> Widget.NULL
                else -> args[0].payload
            }
        )
    }
}
