package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.arithmetic.engine.NoOperatorCandidatesException
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidOperatorArgs
import at.petrak.hexcasting.common.lib.hex.HexArithmetics

/**
 * Represents an Operator with the give pattern as its identifier, a special type of Action that calls a different function depending on the type of its arguments.
 * This exists so that addons can easily define their own overloads to patterns like addition, subtraction, etc.
 */
data class OperationAction(val pattern: HexPattern) : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        return try {
            HexArithmetics.getEngine().run(pattern, env, image, continuation)
        } catch (e: NoOperatorCandidatesException) {
            throw MishapInvalidOperatorArgs(e.args)
        }
    }
}