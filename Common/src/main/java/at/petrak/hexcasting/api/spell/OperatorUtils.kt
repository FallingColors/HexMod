@file:JvmName("OperatorUtils")

package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import com.mojang.datafixers.util.Either
import com.mojang.math.Vector3f
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

fun numOrVec(datum: LegacySpellDatum<*>, reverseIdx: Int): Either<Double, Vec3> =
    when (datum.payload) {
        is Double -> Either.left(datum.payload)
        is Vec3 -> Either.right(datum.payload)
        else -> throw MishapInvalidIota(
            datum,
            reverseIdx,
            "hexcasting.mishap.invalid_value.numvec".asTranslatedComponent
        )
    }

fun numOrList(datum: LegacySpellDatum<*>, reverseIdx: Int): Either<Double, SpellList> =
    when (datum.payload) {
        is Double -> Either.left(datum.payload)
        is SpellList -> Either.right(datum.payload)
        else -> throw MishapInvalidIota(
            datum,
            reverseIdx,
            "hexcasting.mishap.invalid_value.numlist".asTranslatedComponent
        )
    }

fun spellListOf(vararg vs: Any): List<LegacySpellDatum<*>> {
    val out = ArrayList<LegacySpellDatum<*>>(vs.size)
    for (v in vs) {
        out.add(LegacySpellDatum.make(v))
    }
    return out
}

inline fun <reified T : Any> List<LegacySpellDatum<*>>.getChecked(idx: Int, argc: Int = 0): T {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x.payload is T)
        return x.payload
    else
        throw MishapInvalidIota.ofClass(x, if (argc == 0) idx else argc - (idx + 1), T::class.java)
}


inline val Boolean.asSpellResult get() = spellListOf(if (this) 1.0 else 0.0)
inline val Double.asSpellResult get() = spellListOf(this)
inline val Number.asSpellResult get() = spellListOf(this.toDouble())

inline val SpellList.asSpellResult get() = spellListOf(this)
inline val List<LegacySpellDatum<*>>.asSpellResult get() = spellListOf(this)

inline val Widget.asSpellResult get() = spellListOf(this)

inline val BlockPos.asSpellResult get() = spellListOf(Vec3.atCenterOf(this))
inline val Vector3f.asSpellResult get() = spellListOf(Vec3(this))
inline val Vec3.asSpellResult get() = spellListOf(this)

inline val Entity?.asSpellResult get() = spellListOf(this ?: Widget.NULL)
inline val HexPattern.asSpellResult get() = spellListOf(this)

private const val TOLERANCE = 0.0001

fun Any.tolerantEquals(other: Any) = tolerantEquals(other, 64)

private fun Any.tolerantEquals(other: Any, recursionsLeft: Int): Boolean {
    return when {
        this is LegacySpellDatum<*> && other is LegacySpellDatum<*> -> this.payload.tolerantEquals(other.payload, recursionsLeft)
        this is LegacySpellDatum<*> -> this.payload.tolerantEquals(other, recursionsLeft)
        other is LegacySpellDatum<*> -> this.tolerantEquals(other.payload, recursionsLeft)

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
