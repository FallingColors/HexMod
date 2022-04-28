package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import net.minecraft.world.phys.Vec3

object OpBoolNot : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val falsy = args[0].payload == Widget.NULL || args[0].payload == 0.0 || args[0].payload == Vec3.ZERO
        return spellListOf(if (falsy) 1.0 else 0.0)
    }
}
