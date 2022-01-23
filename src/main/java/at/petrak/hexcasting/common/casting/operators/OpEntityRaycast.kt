package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object OpEntityRaycast : ConstManaOperator {
    override val argc = 2
    override val manaCost = 10
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0)
        val look: Vec3 = args.getChecked(1)
        val endp = Operator.raycastEnd(origin, look)

        val entityHitResult = ProjectileUtil.getEntityHitResult(
            ctx.caster,
            origin,
            endp,
            AABB(origin, endp),
            { true },
            Operator.MAX_DISTANCE
        )
        return Operator.spellListOf(
            entityHitResult?.entity ?: Widget.NULL
        )
    }
}