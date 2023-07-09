package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.arithmetic.engine.NoOperatorCandidatesException
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidOperatorArgs
import at.petrak.hexcasting.common.lib.hex.HexArithmetics
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import java.util.*
import java.util.function.Consumer

/**
 * Represents an Operator with the give pattern as its identifier, a special type of Action that calls a different function depending on the type of its arguments.
 * This exists so that addons can easily define their own overloads to patterns like addition, subtraction, etc.
 */
data class OperationAction(val pattern: HexPattern) : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stackList = image.stack
        val stack = Stack<Iota>()
        stack.addAll(stackList)
        val startingLength = stackList.size
        return try {
            val ret: Iterable<Iota> = HexArithmetics.getEngine().run(pattern, stack, startingLength, env)
            ret.forEach(Consumer { e: Iota -> stack.add(e) })
            val image2 = image.copy(stack = stack, opsConsumed = image.opsConsumed + 1) // TODO: maybe let operators figure out how many ops to consume?
            OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
        } catch (e: NoOperatorCandidatesException) {
            throw MishapInvalidOperatorArgs(e.args, e.pattern)
        }
    }
}