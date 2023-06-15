package at.petrak.hexcasting.common.casting.actions.selectors

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

class OpGetEntityAt(val checker: Predicate<Entity>) : ConstMediaAction {
    override val argc = 1
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val pos = args.getVec3(0, argc)
        env.assertVecInRange(pos)
        val aabb = AABB(pos.add(Vec3(-0.5, -0.5, -0.5)), pos.add(Vec3(0.5, 0.5, 0.5)))
        val entitiesGot = env.world.getEntities(null, aabb) {
            OpGetEntitiesBy.isReasonablySelectable(env, it) && checker.test(it)
        }.sortedBy { it.distanceToSqr(pos) }

        val entity = entitiesGot.getOrNull(0)
        return entity.asActionResult
    }
}
