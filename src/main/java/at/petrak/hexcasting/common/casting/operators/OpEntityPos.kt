package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object OpEntityPos : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val e: Entity = args.getChecked(0)
        ctx.assertEntityInRange(e)
        // If this is a player, "expected behavior" is to get the *eye* position so raycasts don't immediately
        // hit the ground.
        return spellListOf(if (e is Player) e.eyePosition else e.position())
    }
}
