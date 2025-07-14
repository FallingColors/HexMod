package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

/**
 * A SimpleOperator that always costs the same amount of media.
 */
interface ConstMediaAction : Action {
    val argc: Int
    val mediaCost: Long
        get() = 0

    fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota>

    fun executeWithOpCount(args: Vector<Iota>, env: CastingEnvironment): CostMediaActionResult {
        val stack = this.execute(args, env)
        return CostMediaActionResult(stack)
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = Vector.VectorBuilder<Iota>()

        if (env.extractMedia(this.mediaCost, true) > 0)
            throw MishapNotEnoughMedia(this.mediaCost)
        if (this.argc > image.stack.size)
            throw MishapNotEnoughArgs(this.argc, image.stack.size)
        val args = image.stack.takeRight(this.argc)
        stack.addAll(image.stack.dropRight(this.argc))
        val result = this.executeWithOpCount(args, env)
        stack.addAll(result.resultStack)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMedia(this.mediaCost))

        val image2 = image.copy(stack = stack.result(), opsConsumed = image.opsConsumed + result.opCount)
        return OperationResult(image2, sideEffects, continuation, HexEvalSounds.NORMAL_EXECUTE)
    }

    data class CostMediaActionResult(val resultStack: Vector<Iota>, val opCount: Long = 1)
}
