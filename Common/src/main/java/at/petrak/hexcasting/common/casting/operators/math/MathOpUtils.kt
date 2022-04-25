package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import com.mojang.datafixers.util.Either
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.phys.Vec3

object MathOpUtils {
    fun GetNumOrVec(datum: SpellDatum<*>, reverseIdx: Int): Either<Double, Vec3> =
        when (datum.payload) {
            is Double -> Either.left(datum.payload)
            is Vec3 -> Either.right(datum.payload)
            else -> throw MishapInvalidIota(
                datum,
                reverseIdx,
                TranslatableComponent("hexcasting.mishap.invalid_value.numvec")
            )
        }
}
