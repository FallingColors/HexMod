package at.petrak.hexcasting.api.casting.arithmetic.operator

import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import java.util.function.Consumer

abstract class Operator
/**
 * @param arity The number of arguments from the stack that this Operator requires; all Operators with the same pattern must have arity.
 * @param accepts A function that should return true if the passed list of Iotas satisfies this Operator's type constraints, and false otherwise.
 */
    (
    @JvmField
    val arity: Int,
    @JvmField
    val accepts: IotaMultiPredicate
) {

    /**
     * Functionally update the image. Return the image and any side effects.
     */
    @Throws(Mishap::class)
    abstract fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult


    companion object {
        /**
         * A helper method to take an iota that you know is of iotaType and returning it as an iota of that type.
         */
        @JvmStatic
        fun <T : Iota?> downcast(iota: Iota, iotaType: IotaType<T>): T {
            check(iota.type === iotaType) { "Attempting to downcast $iota to type: $iotaType" }
            return iota as T
        }
    }
}