package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.entity.player.Player

object OpEntityPos : ConstManaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val e = args.getEntity(0, argc)
        ctx.assertEntityInRange(e)
        // If this is a player, "expected behavior" is to get the *eye* position so raycasts don't immediately
        // hit the ground.
        return (if (e is Player) e.eyePosition else e.position()).asActionResult
    }
}
