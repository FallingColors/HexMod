package at.petrak.hexcasting.api.casting.eval

import net.minecraft.nbt.CompoundTag

/**
 * Information sent back to the client
 */
data class ExecutionClientView(
    val isStackClear: Boolean,
    val resolutionType: ResolvedPatternType,

    // These must be tags so the wrapping of the text can happen on the client
    // otherwise we don't know when to stop rendering
    val stackDescs: List<CompoundTag>,
    val ravenmind: CompoundTag?,
)

