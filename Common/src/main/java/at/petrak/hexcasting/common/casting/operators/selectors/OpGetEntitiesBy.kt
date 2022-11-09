package at.petrak.hexcasting.common.casting.operators.selectors

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getPositiveDouble
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
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

class OpGetEntitiesBy(val checker: Predicate<Entity>, val negate: Boolean) : ConstManaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val pos = args.getVec3(0, argc)
        val radius = args.getPositiveDouble(1, argc)
        ctx.assertVecInRange(pos)

        val aabb = AABB(pos.add(Vec3(-radius, -radius, -radius)), pos.add(Vec3(radius, radius, radius)))
        val entitiesGot = ctx.world.getEntities(null, aabb) {
            isReasonablySelectable(ctx, it)
                    && it.distanceToSqr(pos) <= radius * radius
                    && (checker.test(it) != negate)
        }.sortedBy { it.distanceToSqr(pos) }
        return entitiesGot.map(::EntityIota).asActionResult
    }

    companion object {
        fun isReasonablySelectable(ctx: CastingContext, e: Entity) =
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
