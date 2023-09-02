package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockRaycast : ConstManaOperator {
    override val argc = 2
    override val manaCost = ManaConstants.DUST_UNIT / 100
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0, argc)
        val look: Vec3 = args.getChecked(1, argc)

        ctx.assertVecInRange(origin)

        val blockHitResult = ctx.world.clip(
            ClipContext(
                origin,
                Operator.raycastEnd(origin, look),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                ctx.caster
            )
        )

        return if (blockHitResult.type == HitResult.Type.BLOCK && ctx.isVecInRange(Vec3.atCenterOf(blockHitResult.blockPos))) {
            // the position on the bhr is the position of the specific *hit point*, which is actually on the outside of the block
            // this is weird (for example, casting OpBreakBlock at this position will not break the block we're looking at)
            // so we return the block pos instead
            blockHitResult.blockPos.asSpellResult
        } else {
            Widget.NULL.asSpellResult
        }
    }
}
