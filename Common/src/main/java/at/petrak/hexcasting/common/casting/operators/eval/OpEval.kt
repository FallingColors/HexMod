package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.evaluatable
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag

object OpEval : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        val datum = stack.removeLastOrNull() ?: throw MishapNotEnoughArgs(1, 0)
        val instrs = evaluatable(datum, 0)

        // if not installed already...
        // also, never make a break boundary when evaluating just one pattern
        val newCont =
            if (instrs.left().isPresent || (continuation is SpellContinuation.NotDone && continuation.frame is FrameFinishEval)) {
                continuation
            } else {
                continuation.pushFrame(FrameFinishEval) // install a break-boundary after eval
            }

        val instrsList = instrs.map({ SpellList.LList(0, listOf(PatternIota(it))) }, { it })
        val frame = FrameEvaluate(instrsList, true)
        return OperationResult(stack, userData, listOf(), newCont.pushFrame(frame))
    }
}
