package at.petrak.hex.common.casting.operators.selectors

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.MAX_DISTANCE
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.animal.WaterAnimal
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

class OpGetEntitiesBy(val checker: Predicate<Entity>, val negate: Boolean) : ConstManaOperator {
    override val argc = 2
    override val manaCost = 2000
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val pos = args.getChecked<Vec3>(0)
        val maybeRadius = args.getChecked<Double>(1)
        ctx.assertVecInRange(pos)
        val radius = Mth.clamp(maybeRadius, 0.0, MAX_DISTANCE)

        val aabb = AABB(pos.add(Vec3(-radius, -radius, -radius)), pos.add(Vec3(radius, radius, radius)))
        val entitiesGot = ctx.world.getEntities(
            null,
            aabb
        ) { (checker.test(it) != negate) && it.position().distanceToSqr(ctx.position) <= MAX_DISTANCE * MAX_DISTANCE }
        return spellListOf(entitiesGot)
    }


    companion object {
        @JvmStatic
        fun isAnimal(e: Entity): Boolean = e is Animal || e is WaterAnimal

        @JvmStatic
        fun isMonster(e: Entity): Boolean = e is Monster || e is Slime

        @JvmStatic
        fun isItem(e: Entity): Boolean = e is ItemEntity

        @JvmStatic
        fun isPlayer(e: Entity): Boolean = e is Player

        @JvmStatic
        fun isLiving(e: Entity): Boolean = e is LivingEntity
    }
}