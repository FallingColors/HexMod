package at.petrak.hexcasting.common.casting.actions.selectors

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.animal.WaterAnimal
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

class OpGetEntitiesBy(val checker: Predicate<Entity>, val negate: Boolean) : ConstMediaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val pos = args.getVec3(0, argc)
        val radius = args.getPositiveDouble(1, argc)
        env.assertVecInRange(pos)

        val aabb = AABB(pos.add(Vec3(-radius, -radius, -radius)), pos.add(Vec3(radius, radius, radius)))
        val entitiesGot = env.world.getEntities(null, aabb) {
            isReasonablySelectable(env, it)
                && it.distanceToSqr(pos) <= radius * radius
                && (checker.test(it) != negate)
        }.sortedBy { it.distanceToSqr(pos) }
        return entitiesGot.map(::EntityIota).asActionResult
    }

    companion object {
        fun isReasonablySelectable(ctx: CastingEnvironment, e: Entity) =
            ctx.isEntityInRange(e) && e.isAlive && !e.isSpectator

        @JvmStatic
        fun isAnimal(e: Entity): Boolean = e is Animal || e is WaterAnimal

        @JvmStatic
        fun isMonster(e: Entity): Boolean = e is Enemy

        @JvmStatic
        fun isItem(e: Entity): Boolean = e is ItemEntity

        @JvmStatic
        fun isPlayer(e: Entity): Boolean = e is Player

        @JvmStatic
        fun isLiving(e: Entity): Boolean = e is LivingEntity
    }
}
