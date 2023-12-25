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

        val aabb = circle.executionState!!.bounds // the circle should have an execution state since it's executing this.

        return if (max)
            Vec3(aabb.maxX - 0.5, aabb.maxY - 0.5, aabb.maxZ - 0.5).asActionResult
        else
            Vec3(aabb.minX + 0.5, aabb.minY + 0.5, aabb.minZ + 0.5).asActionResult
    }
}
