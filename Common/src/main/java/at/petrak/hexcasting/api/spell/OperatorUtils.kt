@file:JvmName("OperatorUtils")

package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.iota.*
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import com.mojang.datafixers.util.Either
import com.mojang.math.Vector3f
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun numOrVec(datum: Iota, reverseIdx: Int): Either<Double, Vec3> =
    when (datum) {
        is DoubleIota -> Either.left(datum.double)
        is Vec3Iota -> Either.right(datum.vec3)
        else -> throw MishapInvalidIota(
            datum,
            reverseIdx,
            "hexcasting.mishap.invalid_value.numvec".asTranslatedComponent
        )
    }

fun numOrList(datum: Iota, reverseIdx: Int): Either<Double, SpellList> =
    when (datum) {
        is DoubleIota -> Either.left(datum.double)
        is ListIota -> Either.right(datum.list)
        else -> throw MishapInvalidIota(
            datum,
            reverseIdx,
            "hexcasting.mishap.invalid_value.numlist".asTranslatedComponent
        )
    }

fun List<Iota>.getDouble(idx: Int, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        return x.double
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "double")
    }
}

fun List<Iota>.getEntity(idx: Int, argc: Int = 0): Entity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        return x.entity
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity")
    }
}

fun List<Iota>.getList(idx: Int, argc: Int = 0): SpellList {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ListIota) {
        return x.list
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "list")
    }
}

fun List<Iota>.getPattern(idx: Int, argc: Int = 0): HexPattern {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is PatternIota) {
        return x.pattern
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "pattern")
    }
}

fun List<Iota>.getVec3(idx: Int, argc: Int = 0): Vec3 {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is Vec3Iota) {
        return x.vec3
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "vector")
    }
}

// Helpers

fun List<Iota>.getItemEntity(idx: Int, argc: Int = 0): ItemEntity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is ItemEntity)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.item")
}

fun List<Iota>.getPlayer(idx: Int, argc: Int = 0): ServerPlayer {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is ServerPlayer)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.player")
}

fun List<Iota>.getVillager(idx: Int, argc: Int = 0): Villager {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is Villager)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.villager")
}

fun List<Iota>.getLivingEntity(idx: Int, argc: Int = 0): LivingEntity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is LivingEntity)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.living")
}

fun List<Iota>.getPositiveDoubleB(idx: Int, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        if (0 <= double) {
            return double
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.positive")
}

fun List<Iota>.getDoubleBetween(idx: Int, min: Double, max: Double, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        if (double in min..max) {
            return double
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.between", min, max)
}

fun List<Iota>.getInt(idx: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int")
}

fun List<Iota>.getLong(idx: Int, argc: Int = 0): Long {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToLong()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE) {
            return rounded
        }
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "int") // shh we're lying
}

fun List<Iota>.getPositiveInt(idx: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded > 0) {
            return rounded
        }
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "int.positive")
}

fun List<Iota>.getIntBetween(idx: Int, min: Int, max: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in min..max) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.between", min, max)
}

inline val Boolean.asSpellResult get() = listOf(DoubleIota(if (this) 1.0 else 0.0))
inline val Double.asSpellResult get() = listOf(DoubleIota(this))
inline val Number.asSpellResult get() = listOf(DoubleIota(this.toDouble()))

inline val SpellList.asSpellResult get() = listOf(ListIota(this))
inline val List<Iota>.asSpellResult get() = listOf(ListIota(this))

inline val BlockPos.asSpellResult get() = listOf(Vec3Iota(Vec3.atCenterOf(this)))
inline val Vector3f.asSpellResult get() = listOf(Vec3Iota(Vec3(this)))
inline val Vec3.asSpellResult get() = listOf(Vec3Iota(this))

inline val Entity?.asSpellResult get() = listOf(if (this == null) NullIota.INSTANCE else EntityIota(this))
inline val HexPattern.asSpellResult get() = listOf(PatternIota(this))

private const val TOLERANCE = 0.0001

fun Any.tolerantEquals(other: Any) = tolerantEquals(other, 64)

private fun Any.tolerantEquals(other: Any, recursionsLeft: Int): Boolean {
    return when {
        this is Iota && other is Iota -> this.payload.tolerantEquals(other.payload, recursionsLeft)
        this is Iota -> this.payload.tolerantEquals(other, recursionsLeft)
        other is Iota -> this.tolerantEquals(other.payload, recursionsLeft)

        this is HexPattern && other is HexPattern -> this.angles == other.angles
        this is Double && other is Double -> abs(this - other) < TOLERANCE
        this is Vec3 && other is Vec3 -> this.subtract(other).lengthSqr() < TOLERANCE * TOLERANCE
        this is SpellList && other is SpellList -> {
            val castA = this.toList()
            val castB = other.toList()
            if (castA.size != castB.size || recursionsLeft == 0)
                return false
            for (i in castA.indices)
                if (!castA[i].payload.tolerantEquals(castB[i].payload, recursionsLeft - 1))
                    return false
            true
        }
        this is Entity && other is Entity -> this.uuid == other.uuid
        else -> this == other
    }
}
