package at.petrak.hex.casting.operators

import at.petrak.hex.casting.CastingContext
import at.petrak.hex.casting.SpellDatum
import at.petrak.hex.casting.operators.SpellOperator.Companion.getChecked
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpEntityRaycast : SpellOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0)
        val look: Vec3 = args.getChecked(1)
        val endp = SpellOperator.raycastEnd(origin, look)

        val entityHitResult = ProjectileUtil.getEntityHitResult(
            ctx.caster,
            origin,
            endp,
            AABB(origin, endp),
            { true },
            SpellOperator.MAX_DISTANCE
        )
        return SpellOperator.spellListOf(
            if (entityHitResult != null && entityHitResult.type == HitResult.Type.ENTITY) {
                entityHitResult.entity
            } else {
                Unit
            }
        )
    }
}