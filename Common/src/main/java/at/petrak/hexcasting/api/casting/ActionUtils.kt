@file:JvmName("OperatorUtils")

package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.casting.iota.*
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.function.DoubleUnaryOperator
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun Vector<Iota>.getDouble(idx: Int, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        return x.double
    } else {
        // TODO: I'm not sure this calculation is correct
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "double")
    }
}

fun Vector<Iota>.getEntity(idx: Int, argc: Int = 0): Entity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        return x.entity
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity")
    }
}

fun Vector<Iota>.getList(idx: Int, argc: Int = 0): Vector<Iota> {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ListIota) {
        return x.list
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "list")
    }
}

fun Vector<Iota>.getPattern(idx: Int, argc: Int = 0): HexPattern {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is PatternIota) {
        return x.pattern
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "pattern")
    }
}

fun Vector<Iota>.getVec3(idx: Int, argc: Int = 0): Vec3 {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is Vec3Iota) {
        return x.vec3
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "vector")
    }
}

fun Vector<Iota>.getBool(idx: Int, argc: Int = 0): Boolean {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is BooleanIota) {
        return x.bool
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "boolean")
    }
}

// Helpers

fun Vector<Iota>.getItemEntity(idx: Int, argc: Int = 0): ItemEntity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is ItemEntity)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.item")
}

fun Vector<Iota>.getPlayer(idx: Int, argc: Int = 0): ServerPlayer {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is ServerPlayer)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.player")
}

fun Vector<Iota>.getMob(idx: Int, argc: Int = 0): Mob {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is Mob)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.mob")
}

fun Vector<Iota>.getLivingEntityButNotArmorStand(idx: Int, argc: Int = 0): LivingEntity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is LivingEntity && e !is ArmorStand)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.living")
}

fun Vector<Iota>.getPositiveDouble(idx: Int, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        if (0 <= double) {
            return double
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.positive")
}

fun Vector<Iota>.getPositiveDoubleUnder(idx: Int, max: Double, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        if (0.0 <= double && double < max) {
            return double
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.positive.less", max)
}

fun Vector<Iota>.getPositiveDoubleUnderInclusive(idx: Int, max: Double, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        if (double in 0.0..max) {
            return double
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.positive.less.equal", max)
}

fun Vector<Iota>.getDoubleBetween(idx: Int, min: Double, max: Double, argc: Int = 0): Double {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        if (double in min..max) {
            return double
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double.between", min, max)
}

fun Vector<Iota>.getInt(idx: Int, argc: Int = 0): Int {
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

fun Vector<Iota>.getLong(idx: Int, argc: Int = 0): Long {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToLong()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int") // shh we're lying
}

fun Vector<Iota>.getPositiveInt(idx: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded >= 0) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.positive")
}

fun Vector<Iota>.getPositiveLong(idx: Int, argc: Int = 0): Long {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToLong()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded >= 0) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.positive")
}

fun Vector<Iota>.getPositiveIntUnder(idx: Int, max: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in 0 until max) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.positive.less", max)
}

fun Vector<Iota>.getPositiveIntUnderInclusive(idx: Int, max: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in 0..max) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.positive.less.equal", max)
}

fun Vector<Iota>.getIntBetween(idx: Int, min: Int, max: Int, argc: Int = 0): Int {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in min..max) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.between", min, max)
}

fun Vector<Iota>.getBlockPos(idx: Int, argc: Int = 0): BlockPos {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is Vec3Iota) {
        return BlockPos.containing(x.vec3)
    }

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "vector")
}

fun Vector<Iota>.getNumOrVec(idx: Int, argc: Int = 0): Either<Double, Vec3> {
    val datum = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    return when (datum) {
        is DoubleIota -> Either.left(datum.double)
        is Vec3Iota -> Either.right(datum.vec3)
        else -> throw MishapInvalidIota.of(
            datum,
            if (argc == 0) idx else argc - (idx + 1),
            "numvec"
        )
    }
}

fun Vector<Iota>.getLongOrList(idx: Int, argc: Int = 0): Either<Long, Vector<Iota>> {
    val datum = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (datum is DoubleIota) {
        val double = datum.double
        val rounded = double.roundToLong()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE) {
            return Either.left(rounded)
        }
    } else if (datum is ListIota) {
        return Either.right(datum.list)
    }
    throw MishapInvalidIota.of(
        datum,
        if (argc == 0) idx else argc - (idx + 1),
        "numlist"
    )
}

fun evaluatable(datum: Iota, reverseIdx: Int): Either<Iota, Vector<Iota>> =
    when (datum) {
        is ListIota -> Either.right(datum.list)
        else -> if (datum.executable()) Either.left(datum) else throw MishapInvalidIota(
            datum,
            reverseIdx,
            "hexcasting.mishap.invalid_value.evaluatable".asTranslatedComponent
        )
    }

fun Iota?.orNull() = this ?: NullIota()

// TODO do we make this work on lists
// there should probably be some way to abstract function application over lists, vecs, and numbers,
// and i bet it's fucking monads
fun aplKinnie(operatee: Either<Double, Vec3>, fn: DoubleUnaryOperator): Iota =
    operatee.map(
        { num -> DoubleIota(fn.applyAsDouble(num)) },
        { vec -> Vec3Iota(Vec3(fn.applyAsDouble(vec.x), fn.applyAsDouble(vec.y), fn.applyAsDouble(vec.z))) }
    )

inline val Boolean.asActionResult: Vector<Iota> get() = Vector.from(listOf(BooleanIota(this)))
inline val Double.asActionResult: Vector<Iota> get() = Vector.from(listOf(DoubleIota(this)))
inline val Number.asActionResult: Vector<Iota> get() = Vector.from(listOf(DoubleIota(this.toDouble())))

inline val Vector<Iota>.asActionResult: Vector<Iota> get() = Vector.from(listOf(ListIota(this)))

inline val BlockPos.asActionResult: Vector<Iota> get() = Vector.from(listOf(Vec3Iota(Vec3.atCenterOf(this))))
inline val Vector3f.asActionResult: Vector<Iota> get() = Vector.from(listOf(Vec3Iota(Vec3(this))))
inline val Vec3.asActionResult: Vector<Iota> get() = Vector.from(listOf(Vec3Iota(this)))

inline val Entity?.asActionResult: Vector<Iota> get() = Vector.from(listOf(if (this == null) NullIota() else EntityIota(this)))
inline val HexPattern.asActionResult: Vector<Iota> get() = Vector.from(listOf(PatternIota(this)))
