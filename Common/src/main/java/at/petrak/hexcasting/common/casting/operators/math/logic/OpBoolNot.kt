package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.phys.Vec3

object OpBoolNot : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val payload = args[0].payload
        val falsy = payload == Widget.NULL || payload.tolerantEquals(0.0) || payload.tolerantEquals(Vec3.ZERO) || (payload is SpellList && !payload.nonEmpty)
        return falsy.asSpellResult
    }
}
