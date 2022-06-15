package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB

object OpEntityRaycast : ConstManaAction {
    override val argc = 2
    override val manaCost = ManaConstants.DUST_UNIT / 100
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val origin = args.getVec3(0, argc)
        val look = args.getVec3(1, argc)
        val endp = Action.raycastEnd(origin, look)

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
