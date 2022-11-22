package at.petrak.hexcasting.api.spell.casting.eval

import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.CastingHarness.CastResult
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.hasList
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

// TODO this should probably be a registry too
/**
 * A single frame of evaluation during the execution of a spell.
 *
 * Specifically, an evaluation will keep a stack of these frames.
 * An evaluation with no meta-eval will consist of a single [Evaluate(rest of the pats)] at all times.
 * When an Eval is invoked, we push Evaluate(pats) to the top of the stack.
 *
 * Evaluation is performed by repeatedly popping the top-most (i.e. innermost) frame from the stack,
 * then evaluating that frame (and possibly allowing it to push other frames (e.g. if it's a Hermes)).
 *
 * Once the stack of frames is empty, there are no more computations to run, so we're done.
 *
 */
sealed interface ContinuationFrame {
    /**
     * Step the evaluation forward once.
     * For Evaluate, this consumes one pattern; for ForEach this queues the next iteration of the outer loop.
     * @return the result of this pattern step
     */
    fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingHarness): CastResult

    /**
     * The OpHalt instruction wants us to "jump to" the END of the nearest meta-eval.
     * In other words, we should consume Evaluate frames until we hit a FinishEval or Thoth frame.
     * @return whether the break should stop here, alongside the new stack state (e.g. for finalizing a Thoth)
     */
    fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>>

    /**
     * Serializes this frame. Used for things like delays, where we pause execution.
     */
    fun serializeToNBT(): CompoundTag

    companion object {
        @JvmStatic
        fun fromNBT(tag: CompoundTag, world: ServerLevel): ContinuationFrame {
            return when (tag.getString("type")) {
                "eval" -> FrameEvaluate(
                    HexIotaTypes.LIST.deserialize(
                        tag.getList("patterns", Tag.TAG_COMPOUND),
                        world
                    )!!.list,
                    tag.getBoolean("isMetacasting")
                )

                "end" -> FrameFinishEval
                "foreach" -> FrameForEach(
                    HexIotaTypes.LIST.deserialize(tag.getList("data", Tag.TAG_COMPOUND), world)!!.list,
                    HexIotaTypes.LIST.deserialize(tag.getList("code", Tag.TAG_COMPOUND), world)!!.list,
                    if (tag.hasList("base", Tag.TAG_COMPOUND))
                        HexIotaTypes.LIST.deserialize(tag.getList("base", Tag.TAG_COMPOUND), world)!!.list.toList()
                    else
                        null,
                    HexIotaTypes.LIST.deserialize(
                        tag.getList("accumulator", Tag.TAG_COMPOUND),
                        world
                    )!!.list.toMutableList()
                )

                else -> FrameEvaluate(SpellList.LList(0, listOf()), false)
            }
        }
    }
}
