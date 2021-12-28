package at.petrak.hex.common.casting

import at.petrak.hex.HexUtils
import at.petrak.hex.common.items.ItemSpellbook
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack

/**
 * Info about the moment the spell started being cast.
 */
@JvmRecord
data class CastingContext(
    val caster: ServerPlayer,
    val wandHand: InteractionHand,
) {
    val world: ServerLevel get() = caster.getLevel()

    fun getSpellbook(): ItemStack {
        val handItem =
            caster.getItemInHand(HexUtils.OtherHand(wandHand))
        return if (handItem.item is ItemSpellbook) {
            handItem
        } else {
            throw CastException(CastException.Reason.REQUIRES_SPELLBOOK)
        }
    }
}
