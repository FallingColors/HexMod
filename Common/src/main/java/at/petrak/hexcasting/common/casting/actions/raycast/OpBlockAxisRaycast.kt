package at.petrak.hexcasting.common.casting.actions.raycast

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockAxisRaycast : ConstMediaAction {
    override val argc = 2
    override val mediaCost: Long = MediaConstants.DUST_UNIT / 100
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val origin = args.getVec3(0, argc)
        val look = args.getVec3(1, argc)

        env.assertVecInRange(origin)

        val blockHitResult = env.world.clip(
            ClipContext(
                origin,
                Action.raycastEnd(origin, look),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                env.castingEntity
            )
        )

        return if (blockHitResult.type == HitResult.Type.BLOCK && env.isVecInRange(Vec3.atCenterOf(blockHitResult.blockPos))) {
            blockHitResult.direction.step().asActionResult
        } else {
            listOf(NullIota())
        }
    }
}
