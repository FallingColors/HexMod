package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.Widget
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockAxisRaycast : ConstManaOperator {
    override val argc = 2
    override val manaCost = 10
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0)
        val look: Vec3 = args.getChecked(1)

        val blockHitResult = ctx.world.clip(
            ClipContext(
                origin,
                Operator.raycastEnd(origin, look),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                ctx.caster
            )
        )

        return Operator.spellListOf(
            if (blockHitResult.type == HitResult.Type.BLOCK) {
                Vec3(blockHitResult.direction.step())
            } else {
                Widget.NULL
            }
        )
    }
}