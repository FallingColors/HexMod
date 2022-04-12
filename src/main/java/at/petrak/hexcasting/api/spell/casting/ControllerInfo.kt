package at.petrak.hexcasting.api.spell.casting

import net.minecraft.network.chat.Component

/**
 * Information for the sake of the GUI.
 */
data class ControllerInfo(
    val wasSpellCast: Boolean,
    val hasCastingSound: Boolean,
    val isStackClear: Boolean,
    val wasPrevPatternInvalid: Boolean,
    val stackDesc: List<Component>
)
