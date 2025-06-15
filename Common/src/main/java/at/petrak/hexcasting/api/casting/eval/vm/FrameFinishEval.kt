package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.MapCodec
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

/**
 * A stack marker representing the end of a Hermes evaluation,
 * so that we know when to stop removing frames during a Halt.
 */
object FrameFinishEval : ContinuationFrame {
    // Don't do anything else to the stack, just finish the halt statement.
    override fun breakDownwards(stack: List<Iota>) = true to stack

    // Evaluating it does nothing; it's only a boundary condition.
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingVM
    ): CastResult {
        return CastResult(
            NullIota.INSTANCE,
            continuation,
            null,
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NOTHING,
        )
    }

    override fun size() = 0

    @JvmField
    val TYPE: ContinuationFrame.Type<FrameFinishEval> = object : ContinuationFrame.Type<FrameFinishEval> {
        val CODEC = MapCodec.unit(FrameFinishEval)
        val STREAM_CODEC = StreamCodec.unit<RegistryFriendlyByteBuf, FrameFinishEval>(FrameFinishEval)

        override fun codec(): MapCodec<FrameFinishEval> =
            CODEC

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, FrameFinishEval> =
            STREAM_CODEC

    }

    override val type = TYPE
}
