package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object OpEntityPos : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val e: Entity = args.getChecked(0, argc)
        ctx.assertEntityInRange(e)
        // If this is a player, "expected behavior" is to get the *eye* position so raycasts don't immediately
        // hit the ground.
        return if (e is Player) e.eyePosition.asSpellResult else e.position().asSpellResult
    }
}
