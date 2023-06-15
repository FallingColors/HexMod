package at.petrak.hexcasting.common.casting.actions.raycast

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB

object OpEntityRaycast : ConstMediaAction {
    override val argc = 2
    override val mediaCost = MediaConstants.DUST_UNIT / 100
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val origin = args.getVec3(0, argc)
        val look = args.getVec3(1, argc)
        val endp = Action.raycastEnd(origin, look)

        env.assertVecInRange(origin)

        val entityHitResult = ProjectileUtil.getEntityHitResult(
            env.caster,
            origin,
            endp,
            AABB(origin, endp),
            { true },
            1_000_000.0
        )

        return if (entityHitResult != null && env.isEntityInRange(entityHitResult.entity)) {
            entityHitResult.entity.asActionResult
        } else {
            listOf(NullIota())
        }
    }
}
