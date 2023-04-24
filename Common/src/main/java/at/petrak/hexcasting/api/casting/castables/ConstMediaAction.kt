package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag

/**
 * A SimpleOperator that always costs the same amount of media.
 */
interface ConstMediaAction : Action {
    val argc: Int
    val mediaCost: Int
        get() = 0

    fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota>

    fun executeWithOpCount(args: List<Iota>, env: CastingEnvironment): CostMediaActionResult {
        val stack = this.execute(args, env)
        return CostMediaActionResult(stack)
    }

    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        repeat(this.argc) { stack.removeLast() }
        val result = this.executeWithOpCount(args, env)
        stack.addAll(result.resultStack)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMedia(this.mediaCost))

        return OperationResult(stack, userData, sideEffects, continuation, result.opCount)
    }

    data class CostMediaActionResult(val resultStack: List<Iota>, val opCount: Long = 1)
}
