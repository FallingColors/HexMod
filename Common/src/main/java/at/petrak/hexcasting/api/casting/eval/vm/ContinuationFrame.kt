package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexContinuationTypes
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import java.util.function.Function
import java.util.function.Supplier

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
interface ContinuationFrame {
    /**
     * Step the evaluation forward once.
     * For Evaluate, this consumes one pattern; for ForEach this queues the next iteration of the outer loop.
     * @return the result of this pattern step
     */
    fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult

    /**
     * The OpHalt instruction wants us to "jump to" the END of the nearest meta-eval.
     * In other words, we should consume Evaluate frames until we hit a FinishEval or Thoth frame.
     * @return whether the break should stop here, alongside the new stack state (e.g. for finalizing a Thoth)
     */
    fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>>

    /**
     * Return the number of iotas contained inside this frame, used for determining whether it is valid to serialise.
     */
    fun size(): Int

    val type: Type<*>

    interface Type<U : ContinuationFrame> {

        fun codec(): MapCodec<U>

        fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, U>
        
        companion object {
            //TODO if doesn't exist use `FrameEvaluate(SpellList.LList(0, listOf()), false)` as default value
            @JvmStatic
            val TYPED_CODEC: Codec<ContinuationFrame> = Codec.lazyInitialized<ContinuationFrame> {
                IXplatAbstractions.INSTANCE
                    .continuationTypeRegistry
                    .byNameCodec()
                    .dispatch<ContinuationFrame>(
                        "type",
                        ContinuationFrame::type,
                        Type<*>::codec
                    )
            }

            @JvmStatic
            val TYPED_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ContinuationFrame> = ByteBufCodecs
                .registry(HexRegistries.CONTINUATION_TYPE)
                .dispatch<ContinuationFrame>(
                    ContinuationFrame::type,
                    Type<*>::streamCodec
                )
        }
    }

    companion object {

        /**
         * This method attempts to find the type from the `type` key.
         * See [ContinuationFrame.serializeToNBT] for the storage format.
         *
         * @return `null` if it cannot get the type.
         */
        private fun getTypeFromTag(tag: CompoundTag): Type<*>? {
            if (!tag.contains(HexContinuationTypes.KEY_TYPE, Tag.TAG_STRING.toInt())) {
                return null
            }

            val typeKey = tag.getString(HexContinuationTypes.KEY_TYPE)

            val typeLoc = ResourceLocation.tryParse(typeKey)
                ?: return null

            return HexContinuationTypes.REGISTRY[typeLoc]
        }
    }
}
