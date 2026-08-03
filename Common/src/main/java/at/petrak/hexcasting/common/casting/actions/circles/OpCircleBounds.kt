package at.petrak.hexcasting.common.casting.actions.circles

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import net.minecraft.world.phys.Vec3

class OpCircleBounds(val max: Boolean) : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val circle = env.impetus ?: throw MishapNoSpellCircle()

        val boundsBox = circle.executionState!!.bounds // the circle should have an execution state since it's executing this.

        return if (max)
            boundsBox.max().asActionResult
        else
            boundsBox.min().asActionResult
    }
}
