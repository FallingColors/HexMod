package at.petrak.hexcasting.api.spell.casting

import net.minecraft.nbt.CompoundTag

/**
 * Information for the sake of the GUI.
 */
data class ControllerInfo(
    val makesCastSound: Boolean,
    val isStackClear: Boolean,
    val resolutionType: ResolvedPatternType,
    val stack: List<CompoundTag>,
    val parenthesized: List<CompoundTag>,
    val ravenmind: CompoundTag?,
    val parenCount: Int,
)
