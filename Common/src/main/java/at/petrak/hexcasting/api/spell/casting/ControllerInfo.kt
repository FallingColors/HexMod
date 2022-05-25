package at.petrak.hexcasting.api.spell.casting

import net.minecraft.network.chat.Component

/**
 * Information for the sake of the GUI.
 */
data class ControllerInfo(
    val makesCastSound: Boolean,
    val isStackClear: Boolean,
    val resolutionType: ResolvedPatternType,
    val stackDesc: List<Component>
)
