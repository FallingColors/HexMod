package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.common.misc.PlayerPositionRecorder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

object OpEntityVelocity : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val e: Entity = args.getChecked(0)
        ctx.assertEntityInRange(e)

        // Player velocity is jank. Really jank. This is the best we can do.
        if (e is ServerPlayer) {
            val prevPosition = PlayerPositionRecorder.getLastPosition(e)
            return spellListOf(e.position().subtract(prevPosition))
        }

        return spellListOf(e.deltaMovement)
    }
}
