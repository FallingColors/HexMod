package at.petrak.hexcasting.common.casting.actions.raycast

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

object OpEntityRaycast : ConstMediaAction {
    override val argc = 2
    override val mediaCost: Long = MediaConstants.DUST_UNIT / 100
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val origin = args.getVec3(0, argc)
        val look = args.getVec3(1, argc)
        val endp = Action.raycastEnd(origin, look)

        env.assertVecInRange(origin)

        val entityHitResult = getEntityHitResult(
            env.caster,
            env.world,
            origin,
            endp,
            AABB(origin, endp),
            { true },
            1_000_000.0
        )

        return if (entityHitResult != null && env.isEntityInRange(entityHitResult.entity)) {
            entityHitResult.entity.asActionResult
        } else {
            listOf(NullIota())
        }
    }

    fun getEntityHitResult(
            entity: Entity?, level: Level, startPos: Vec3, endPos: Vec3,
            aabb: AABB, isValid: Predicate<Entity>, maxSqrLength: Double): EntityHitResult? {
        var sqrLength = maxSqrLength
        var hitEntity: Entity? = null
        var hitPos: Vec3? = null
        val allValidInAABB: Iterator<*> = level.getEntities(entity, aabb, isValid).iterator()

        while (allValidInAABB.hasNext()) {
            val nextEntity = allValidInAABB.next() as Entity
            val hitBox = nextEntity.boundingBox.inflate(nextEntity.pickRadius.toDouble())
            val overlapBox = hitBox.clip(startPos, endPos)
            if (hitBox.contains(startPos)) {
                if (sqrLength >= 0.0) {
                    hitEntity = nextEntity
                    hitPos = overlapBox.orElse(startPos)
                    sqrLength = 0.0
                }
            } else if (overlapBox.isPresent) {
                val maybePos = overlapBox.get()
                val sqrDist = startPos.distanceToSqr(maybePos)
                if (sqrDist < sqrLength || sqrLength == 0.0) {
                    if (nextEntity.rootVehicle === entity?.rootVehicle) {
                        if (sqrLength == 0.0) {
                            hitEntity = nextEntity
                            hitPos = maybePos
                        }
                    } else {
                        hitEntity = nextEntity
                        hitPos = maybePos
                        sqrLength = sqrDist
                    }
                }
            }
        }
        return if (hitEntity == null) {
            null
        } else EntityHitResult(hitEntity, hitPos!!) // hitEntity != null <=> hitPos != null
    }
}
