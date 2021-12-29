package at.petrak.hex.common.casting.operators

import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.SpellOperator.Companion.spellListOf
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object OpEntityPos : SimpleOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val e: Entity = args.getChecked(0)
        // If this is a player, "expected behavior" is to get the *eye* position so raycasts don't immediately
        // hit the ground.
        return spellListOf(if (e is Player) e.eyePosition else e.position())
    }
}