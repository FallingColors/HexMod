package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellWidget
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockRaycast : ConstManaOperator {
    override val argc = 2
    override val manaCost = 10
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0)
        val look: Vec3 = args.getChecked(1)

        val blockHitResult = ctx.world.clip(
            ClipContext(
                origin,
                SpellOperator.raycastEnd(origin, look),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                ctx.caster
            )
        )

        return SpellOperator.spellListOf(
            if (blockHitResult.type == HitResult.Type.BLOCK) {
                // the position on the bhr is the position of the specific *hit point*, which is actually on the outside of the block
                // this is weird (for example, casting OpBreakBlock at this position will not break the block we're looking at)
                // so we return the block pos instead
                Vec3(
                    blockHitResult.blockPos.x.toDouble(),
                    blockHitResult.blockPos.y.toDouble(),
                    blockHitResult.blockPos.z.toDouble()
                )
            } else {
                SpellWidget.NULL
            }
        )
    }
}