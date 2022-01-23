package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastException
import com.mojang.datafixers.util.Either
import net.minecraft.world.phys.Vec3

object MathOpUtils {
    fun GetNumOrVec(datum: SpellDatum<*>): Either<Double, Vec3> =
        when (datum.payload) {
            is Double -> Either.left(datum.payload)
            is Vec3 -> Either.right(datum.payload)
            else -> throw CastException(CastException.Reason.OP_WRONG_TYPE, Either::class.java, datum.payload)
        }
}