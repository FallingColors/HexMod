package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object OpEntityRaycast : ConstManaOperator {
    override val argc = 2
    override val manaCost = ManaConstants.DUST_UNIT / 100
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0, argc)
        val look: Vec3 = args.getChecked(1, argc)
        val endp = Operator.raycastEnd(origin, look)

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
            entityHitResult.entity.asSpellResult
        } else {
            null.asSpellResult
        }
    }
}
