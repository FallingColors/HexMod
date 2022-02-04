package at.petrak.hexcasting.common.casting

import net.minecraft.network.chat.Component

/**
 * Information for the sake of the GUI.
 */
data class ControllerInfo(
    val wasSpellCast: Boolean,
    val isStackEmpty: Boolean,
    val wasPrevPatternInvalid: Boolean,
    val stackDesc: List<Component>
)