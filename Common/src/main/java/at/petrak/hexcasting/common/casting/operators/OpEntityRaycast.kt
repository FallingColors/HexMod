package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.Action
import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB

object OpEntityRaycast : ConstMediaAction {
    override val argc = 2
    override val mediaCost = MediaConstants.DUST_UNIT / 100
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val origin = args.getVec3(0, argc)
        val look = args.getVec3(1, argc)
        val endp = Action.raycastEnd(origin, look)

        ctx.assertVecInRange(origin)

        val entityHitResult = ProjectileUtil.getEntityHitResult(
            ctx.caster,
            origin,
            endp,
            AABB(origin, endp),
            { true },
            1_000_000.0
        )

        return if (entityHitResult != null && ctx.isEntityInRange(entityHitResult.entity)) {
            entityHitResult.entity.asActionResult
        } else {
            listOf(NullIota())
        }
    }
}
