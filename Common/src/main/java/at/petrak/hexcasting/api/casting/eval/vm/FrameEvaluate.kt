package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

/**
 * A list of patterns to be evaluated in sequence.
 * @property list the *remaining* list of patterns to be evaluated
 * @property isMetacasting only for sound effects, if this is being cast from a hermes / iris
 */
data class FrameEvaluate(val list: SpellList, val isMetacasting: Boolean) : ContinuationFrame {
    // Discard this frame and keep discarding frames.
    override fun breakDownwards(stack: List<Iota>) = false to stack

    // Step the list of patterns, evaluating a single one.
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingVM
    ): CastResult {
        // If there are patterns left...
        return if (list.nonEmpty) {
            val newCont = if (list.cdr.nonEmpty) { // yay TCO
                // ...enqueue the evaluation of the rest of the patterns...
                continuation.pushFrame(FrameEvaluate(list.cdr, this.isMetacasting))
            } else continuation
            // ...before evaluating the first one in the list.
            val update = harness.executeInner(list.car, level, newCont)
            if (this.isMetacasting && update.sound != HexEvalSounds.MISHAP) {
                update.copy(sound = HexEvalSounds.HERMES)
            } else {
                update
            }
        } else {
            // If there are no patterns (e.g. empty Hermes), just return OK.
            CastResult(ListIota(list), continuation, null, listOf(), ResolvedPatternType.EVALUATED, HexEvalSounds.HERMES)
        }
    }

    override fun serializeToNBT() = NBTBuilder {
        "patterns" %= list.serializeToNBT()
        "isMetacasting" %= isMetacasting
    }

    override fun size() = list.size()

    override val type: ContinuationFrame.Type<*> = TYPE

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameEvaluate> = object : ContinuationFrame.Type<FrameEvaluate> {
            override fun deserializeFromNBT(tag: CompoundTag, world: ServerLevel): FrameEvaluate {
                return FrameEvaluate(
                    HexIotaTypes.LIST.deserialize(
                        tag.getList("patterns", Tag.TAG_COMPOUND),
                        world
                    )!!.list,
                    tag.getBoolean("isMetacasting"))
            }

        }
    }
}