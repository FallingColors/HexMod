package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag

object OpForEach : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs = stack.getList(stack.lastIndex - 1)
        val datums = stack.getList(stack.lastIndex)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        val frame = FrameForEach(datums, instrs, null, mutableListOf())

        return OperationResult(
            stack,
            userData,
            listOf(),
            continuation.pushFrame(frame),
            1
        )
    }
}
