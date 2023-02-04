package at.petrak.hexcasting.api.casting.eval

import net.minecraft.network.chat.Component

/**
 * Information sent back to the client
 */
data class ExecutionClientView(
    val isStackClear: Boolean,
    val resolutionType: ResolvedPatternType,

    val stackDescs: List<Component>,
    val ravenmind: Component?,
)

