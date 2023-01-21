package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.misc.PlayerPositionRecorder
import net.minecraft.server.level.ServerPlayer

object OpEntityVelocity : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val e = args.getEntity(0, argc)
        ctx.assertEntityInRange(e)

        // Player velocity is jank. Really jank. This is the best we can do.
        if (e is ServerPlayer) {
            return PlayerPositionRecorder.getMotion(e).asActionResult
        }

        return e.deltaMovement.asActionResult
    }
}
