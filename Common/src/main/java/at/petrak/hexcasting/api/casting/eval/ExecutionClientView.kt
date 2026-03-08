package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Information sent back to the client
 */
data class ExecutionClientView(
    val isStackClear: Boolean,
    val resolutionType: ResolvedPatternType,

    // These must be tags so the wrapping of the text can happen on the client
    // otherwise we don't know when to stop rendering
    val stackDescs: List<Iota>,
    val ravenmind: CompoundTag?
) {

    companion object {
        @JvmStatic
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ExecutionClientView> =
            StreamCodec.composite(
                ByteBufCodecs.BOOL, ExecutionClientView::isStackClear,
                ByteBufCodecs.idMapper<ResolvedPatternType>(
                    { ResolvedPatternType.entries[it] },
                    ResolvedPatternType::ordinal
                ), ExecutionClientView::resolutionType,
                IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), ExecutionClientView::stackDescs,
                ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).map(
                    { it.getOrNull() },
                    Optional<CompoundTag>::ofNullable
                ), ExecutionClientView::ravenmind,
                ::ExecutionClientView
            )
    }
}

