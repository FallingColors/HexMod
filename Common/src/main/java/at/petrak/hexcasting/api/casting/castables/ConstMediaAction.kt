package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.OperationResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

/**
 * A SimpleOperator that always costs the same amount of media.
 */
interface ConstMediaAction : Action {
    val argc: Int
    val mediaCost: Int
        get() = 0

    fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota>

    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingEnvironment
    ): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        repeat(this.argc) { stack.removeLast() }
        val newData = this.execute(args, ctx)
        stack.addAll(newData)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMedia(this.mediaCost))

        return OperationResult(continuation, stack, ravenmind, sideEffects)
    }
}
