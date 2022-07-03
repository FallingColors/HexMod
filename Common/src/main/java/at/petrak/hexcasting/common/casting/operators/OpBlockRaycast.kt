package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockRaycast : ConstManaAction {
    override val argc = 2
    override val manaCost = ManaConstants.DUST_UNIT / 100
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val origin = args.getVec3(0, argc)
        val look = args.getVec3(1, argc)

        val blockHitResult = ctx.world.clip(
            ClipContext(
                origin,
                Action.raycastEnd(origin, look),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                ctx.caster
            )
        )

        return if (blockHitResult.type == HitResult.Type.BLOCK && ctx.isVecInRange(Vec3.atCenterOf(blockHitResult.blockPos))) {
            // the position on the bhr is the position of the specific *hit point*, which is actually on the outside of the block
            // this is weird (for example, casting OpBreakBlock at this position will not break the block we're looking at)
            // so we return the block pos instead
            // TODO some action that has the "weird" version?
            blockHitResult.blockPos.asActionResult
        } else {
            listOf(NullIota())
        }
    }
}
